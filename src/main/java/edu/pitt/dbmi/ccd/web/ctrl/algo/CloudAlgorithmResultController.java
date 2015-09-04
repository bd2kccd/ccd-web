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
package edu.pitt.dbmi.ccd.web.ctrl.algo;

import edu.pitt.dbmi.ccd.commons.file.FilePrint;
import edu.pitt.dbmi.ccd.commons.file.info.BasicFileInfo;
import edu.pitt.dbmi.ccd.commons.file.info.FileInfos;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.ResultFileInfo;
import edu.pitt.dbmi.ccd.web.model.SelectedFiles;
import edu.pitt.dbmi.ccd.web.model.d3.Node;
import edu.pitt.dbmi.ccd.web.service.cloud.CloudResultFileService;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Aug 21, 2015 8:41:47 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Profile("desktop")
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "cloud/algorithm/results")
public class CloudAlgorithmResultController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudAlgorithmResultController.class);

    private final CloudResultFileService cloudResultFileService;

    @Autowired(required = true)
    public CloudAlgorithmResultController(CloudResultFileService cloudResultFileService) {
        this.cloudResultFileService = cloudResultFileService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runResultAction(
            final SelectedFiles selectedFiles,
            @RequestParam(value = "action") final String action,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<String> fileNames = selectedFiles.getFiles();
        switch (action) {
            case "delete":
                List<String> filesOnCloud = new LinkedList<>();
                fileNames.forEach(fileName -> {
                    Path file = Paths.get(appUser.getResultDirectory(), fileName);
                    if (Files.exists(file)) {
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException exception) {
                            LOGGER.error(exception.getMessage());
                        }
                    } else {
                        filesOnCloud.add(fileName);
                    }
                });
                String username = appUser.getUsername();
                filesOnCloud.forEach(fileName -> {
                    cloudResultFileService.deleteFile(username, fileName);
                });
                break;
        }

        return "redirect:/cloud/algorithm/results";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showRunResultsView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        try {
            List<Path> list = FileInfos.listDirectory(Paths.get(appUser.getResultDirectory()), false);
            List<Path> files = list.stream().filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());

            List<ResultFileInfo> clouldData = cloudResultFileService.getUserResultFiles(appUser.getUsername());

            ResultFileInfo[] fileInfos = new ResultFileInfo[files.size() + clouldData.size()];

            List<BasicFileInfo> results = FileInfos.listBasicPathInfo(files);
            int index = 0;
            for (BasicFileInfo result : results) {
                String fileName = result.getFilename();

                ResultFileInfo fileInfo = new ResultFileInfo();
                fileInfo.setCreationDate(FilePrint.fileTimestamp(result.getCreationTime()));
                fileInfo.setFileName(result.getFilename());
                fileInfo.setSize(FilePrint.humanReadableSize(result.getSize(), true));
                fileInfo.setRawCreationDate(result.getCreationTime());
                fileInfo.setError(fileName.startsWith("error"));

                fileInfos[index++] = fileInfo;
            }

            for (ResultFileInfo cloudInfo : clouldData) {
                fileInfos[index++] = cloudInfo;
            }

            // sort
            Arrays.sort(fileInfos, Collections.reverseOrder());
            model.addAttribute("itemList", Arrays.asList(fileInfos));
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
            model.addAttribute("itemList", new LinkedList<>());
        }

        return ALGORITHM_RESULTS_VIEW;
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String deleteResultFile(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser) {
        cloudResultFileService.deleteFile(appUser.getUsername(), filename);

        return "redirect:/cloud/algorithm/results";
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void downloadResultFileFromCloud(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", filename);
        response.setHeader(headerKey, headerValue);

        byte[] cloudData = cloudResultFileService.downloadFile(appUser.getUsername(), filename);

        try (ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(cloudData));
                WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            while (inputChannel.read(buffer) != -1) {
                // prepare the buffer to be drained
                buffer.flip();
                // write to the channel, may block
                outputChannel.write(buffer);
                // If partial transfer, shift remainder down
                // If buffer is empty, same as doing clear()
                buffer.compact();
            }
            // EOF will leave buffer in fill state
            buffer.flip();
            // make sure the buffer is fully drained.
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to download file '%s'.", filename), exception);
        }
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String showResultError(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {

        List<String> errors = new LinkedList<>();
        byte[] cloudData = cloudResultFileService.downloadFile(appUser.getUsername(), filename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                errors.add(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        model.addAttribute("errors", errors);

        return ALGORITHM_RESULT_ERROR_VIEW;
    }

    @RequestMapping(value = "plot", method = RequestMethod.GET)
    public String showPlotFromCloud(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        byte[] cloudData = cloudResultFileService.downloadFile(appUser.getUsername(), filename);
        Map<String, String> parameters = new TreeMap<>();
        Pattern equalDelim = Pattern.compile("=");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
            boolean isParamters = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();

                if (isParamters) {
                    String[] data = equalDelim.split(line);
                    if (data.length == 2) {
                        parameters.put(data[0].trim(), data[1].trim());
                    } else {
                        break;
                    }
                } else if ("Graph Parameters:".equals(line)) {
                    isParamters = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", filename), exception);
        }

        model.addAttribute("plot", filename);
        model.addAttribute("link", "/cloud/algorithm/results/d3graph?file=" + filename);
        model.addAttribute("parameters", parameters);

        return PLOT_VIEW;
    }

    @RequestMapping(value = "d3graph", method = RequestMethod.GET)
    public String showD3GraphFromCloud(
            @RequestParam(value = "file") final String filename,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<Node> links = new LinkedList<>();
        Pattern space = Pattern.compile("\\s+");
        byte[] cloudData = cloudResultFileService.downloadFile(appUser.getUsername(), filename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cloudData), Charset.defaultCharset()))) {
            boolean isData = false;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (isData) {
                    String[] data = space.split(line);
                    if (data.length == 4) {
                        links.add(new Node(data[1], data[3], data[2]));
                    }
                } else if ("Graph Edges:".equals(line)) {
                    isData = true;
                }
            }
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file '%s'.", filename), exception);
        }

        model.addAttribute("data", links);

        return D3_GRAPH_VIEW;
    }

}
