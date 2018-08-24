/*
 * Copyright (C) 2018 University of Pittsburgh.
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
package edu.pitt.dbmi.causal.web.service.file;

import edu.pitt.dbmi.causal.web.model.file.FileDetailForm;
import edu.pitt.dbmi.causal.web.util.DateFormatUtils;
import edu.pitt.dbmi.causal.web.util.FilePrint;
import edu.pitt.dbmi.ccd.db.code.FileFormatCodes;
import edu.pitt.dbmi.ccd.db.entity.File;
import edu.pitt.dbmi.ccd.db.entity.FileFormat;
import edu.pitt.dbmi.ccd.db.entity.TetradDataFile;
import edu.pitt.dbmi.ccd.db.entity.TetradVariableFile;
import edu.pitt.dbmi.ccd.db.service.FileService;
import edu.pitt.dbmi.ccd.db.service.TetradDataFileService;
import edu.pitt.dbmi.ccd.db.service.TetradVariableFileService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Jun 4, 2018 12:09:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class FileDetailService {

    private final FileService fileService;
    private final TetradDataFileService tetradDataFileService;
    private final TetradVariableFileService tetradVariableFileService;

    @Autowired
    public FileDetailService(FileService fileService, TetradDataFileService tetradDataFileService, TetradVariableFileService tetradVariableFileService) {
        this.fileService = fileService;
        this.tetradDataFileService = tetradDataFileService;
        this.tetradVariableFileService = tetradVariableFileService;
    }

    public File updateDetails(FileDetailForm fileDetailForm, File file) {
        file.setName(fileDetailForm.getName());
        file.setDescription(fileDetailForm.getDescription());

        return fileService.getRepository().save(file);
    }

    public FileDetailForm getFileDetailForm(File file) {
        FileDetailForm form = new FileDetailForm();
        form.setName(file.getName());
        form.setDescription(file.getDescription());

        return form;
    }

    public Map<String, String> getShortFileDetails(File file) {
        Map<String, String> fileInfo = new LinkedHashMap<>();
        fileInfo.put("File Name", file.getFileName());
        fileInfo.put("Name", file.getName());
        fileInfo.put("Created", DateFormatUtils.format(file.getCreationTime()));
        fileInfo.put("Size", FilePrint.toHumanReadableSize(file.getFileSize(), false));

        return fileInfo;
    }

    public Map<String, String> getCategorizationDetails(File file) {
        Map<String, String> catInfo = new LinkedHashMap<>();

        FileFormat fileFormat = file.getFileFormat();
        if (fileFormat != null) {
            catInfo.put("File Format", fileFormat.getName());

            switch (fileFormat.getCode()) {
                case FileFormatCodes.TETRAD_TAB:
                    TetradDataFile dataFile = tetradDataFileService.getRepository()
                            .findByFile(file);
                    if (dataFile != null) {
                        catInfo.put("Number of Variables", String.valueOf(dataFile.getNumOfVars()));
                        catInfo.put("Number of Cases", String.valueOf(dataFile.getNumOfCases()));
                        catInfo.put("Delimiter", dataFile.getDataDelimiter().getName());
                        catInfo.put("Variable Type", dataFile.getVariableType().getName());
                        catInfo.put("Has Header", dataFile.isHasHeader() ? "Yes" : "No");
                        catInfo.put("Quote Character", String.valueOf(dataFile.getQuoteChar()));
                        catInfo.put("Missing Value Marker", dataFile.getMissingMarker());
                        catInfo.put("Comment Marker", dataFile.getCommentMarker());
                    }
                    break;
                case FileFormatCodes.TETRAD_VAR:
                    TetradVariableFile varFile = tetradVariableFileService.getRepository()
                            .findByFile(file);
                    if (varFile != null) {
                        catInfo.put("Number of Variables", String.valueOf(varFile.getNumOfVars()));
                    }
                    break;
            }
        }

        return catInfo;
    }

}