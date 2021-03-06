/*
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.service.data;

import edu.pitt.dbmi.ccd.commons.file.MessageDigestHash;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.db.entity.AnnotationTarget;
import edu.pitt.dbmi.ccd.db.entity.DataFile;
import edu.pitt.dbmi.ccd.db.entity.DataFileInfo;
import edu.pitt.dbmi.ccd.db.entity.UserAccount;
import edu.pitt.dbmi.ccd.db.service.AnnotationTargetService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.UserAccountService;
import edu.pitt.dbmi.ccd.web.model.data.ResumableChunk;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Sep 20, 2015 7:58:08 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class DataFileManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFileManagerService.class);

    final String workspace;

    final String dataFolder;

    private final UserAccountService userAccountService;

    private final DataFileService dataFileService;

    private final AnnotationTargetService annotationTargetService;

    @Autowired
    public DataFileManagerService(
            @Value("${ccd.server.workspace}") String workspace,
            @Value("${ccd.folder.data:data}") String dataFolder,
            UserAccountService userAccountService,
            DataFileService dataFileService,
            AnnotationTargetService annotationTargetService) {
        this.workspace = workspace;
        this.dataFolder = dataFolder;
        this.userAccountService = userAccountService;
        this.dataFileService = dataFileService;
        this.annotationTargetService = annotationTargetService;
    }

    public boolean isSupported(ResumableChunk chunk) {
        return true;
    }

    public boolean chunkExists(ResumableChunk chunk, String username) throws IOException {
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());
        long chunkSize = chunk.getResumableChunkSize();

        Path chunkFile = Paths.get(workspace, username, dataFolder, identifier, chunkNumber);
        if (Files.exists(chunkFile)) {
            long size = (Long) Files.getAttribute(chunkFile, "basic:size");
            return size == chunkSize;
        }

        return false;
    }

    public void storeChunk(ResumableChunk chunk, String username) throws IOException {
        String identifier = chunk.getResumableIdentifier();
        String chunkNumber = Integer.toString(chunk.getResumableChunkNumber());
        InputStream inputStream = chunk.getFile().getInputStream();

        Path chunkFile = Paths.get(workspace, username, dataFolder, identifier, chunkNumber);
        if (Files.notExists(chunkFile)) {
            try {
                Files.createDirectories(chunkFile);
            } catch (IOException exception) {
                LOGGER.error(exception.getMessage());
            }
        }
        Files.copy(inputStream, chunkFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public boolean allChunksUploaded(ResumableChunk chunk, String username) {
        String identifier = chunk.getResumableIdentifier();
        int numOfChunks = chunk.getResumableTotalChunks();

        for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
            if (!Files.exists(Paths.get(workspace, username, dataFolder, identifier, Integer.toString(chunkNo)))) {
                return false;
            }
        }

        return true;
    }

    private String saveDataFile(Path file, String username) throws IOException {
        String md5checkSum = MessageDigestHash.computeMD5Hash(file);

        BasicFileInfo fileInfo = FileInfos.basicPathInfo(file);
        String name = fileInfo.getFilename();
        String absolutePath = fileInfo.getAbsolutePath().toString();
        Date creationTime = new Date(fileInfo.getCreationTime());
        Date lastModifiedTime = new Date(fileInfo.getLastModifiedTime());
        long fileSize = fileInfo.getSize();

        UserAccount userAccount = userAccountService.findByUsername(username);

        synchronized (dataFileService) {
            DataFile dataFile = dataFileService.findByAbsolutePathAndName(absolutePath, name);
            AnnotationTarget annotationTarget;
            if (dataFile == null) {
                dataFile = new DataFile();
                dataFile.setAbsolutePath(absolutePath);
                dataFile.setName(name);
                dataFile.setUserAccounts(Collections.singleton(userAccount));

                // create new Annotatable entity
                annotationTarget = new AnnotationTarget(userAccount, dataFile.getName(), dataFile);
            } else {
                // Get annotatable entity from Data File
                annotationTarget = annotationTargetService.findByDataFile(dataFile);
                if (annotationTarget == null) {
                    annotationTarget = new AnnotationTarget(userAccount, dataFile.getName(), dataFile);
                }
            }
            dataFile.setCreationTime(creationTime);
            dataFile.setFileSize(fileSize);
            dataFile.setLastModifiedTime(lastModifiedTime);

            DataFileInfo dataFileInfo = dataFile.getDataFileInfo();
            if (dataFileInfo == null) {
                dataFileInfo = new DataFileInfo();
                dataFileInfo.setMd5checkSum(md5checkSum);

                dataFile.setDataFileInfo(dataFileInfo);
            } else {
                if (!md5checkSum.equals(dataFileInfo.getMd5checkSum())) {
                    dataFileInfo.setFileDelimiter(null);
                    dataFileInfo.setMd5checkSum(md5checkSum);
                    dataFileInfo.setMissingValue(null);
                    dataFileInfo.setNumOfColumns(null);
                    dataFileInfo.setNumOfRows(null);
                    dataFileInfo.setVariableType(null);
                }
            }

            dataFileService.saveDataFile(dataFile);
            annotationTargetService.save(annotationTarget);
        }

        return md5checkSum;
    }

    /**
     * Delete folder/directory. If the directory is non-empty, all the files and
     * folder in the directory will be deleted.
     *
     * @param path path to folder to be deleted
     * @throws IOException
     */
    private void deleteNonEmptyDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                if (exception == null) {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exception;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public String mergeDeleteSave(ResumableChunk chunk, String username) throws IOException {
        String fileName = chunk.getResumableFilename();
        int numOfChunks = chunk.getResumableTotalChunks();
        String identifier = chunk.getResumableIdentifier();

        Path newFile = Paths.get(workspace, username, dataFolder, fileName);
        Files.deleteIfExists(newFile); // delete the existing file
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile.toFile(), false))) {
            for (int chunkNo = 1; chunkNo <= numOfChunks; chunkNo++) {
                Path chunkPath = Paths.get(workspace, username, dataFolder, identifier, Integer.toString(chunkNo));
                Files.copy(chunkPath, bos);
            }
        }

        String md5checkSum = saveDataFile(newFile, username);
        try {
            deleteNonEmptyDir(Paths.get(workspace, username, dataFolder, identifier));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return md5checkSum;
    }

}
