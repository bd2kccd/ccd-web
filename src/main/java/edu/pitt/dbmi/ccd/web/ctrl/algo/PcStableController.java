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

import edu.pitt.dbmi.ccd.db.service.DataFileInfoService;
import edu.pitt.dbmi.ccd.db.service.DataFileService;
import edu.pitt.dbmi.ccd.db.service.FileDelimiterService;
import edu.pitt.dbmi.ccd.db.service.VariableTypeService;
import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.domain.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.PcStableRunInfo;
import edu.pitt.dbmi.ccd.web.service.AlgorithmService;
import edu.pitt.dbmi.ccd.web.service.DataService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Apr 4, 2015 8:09:20 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "/algorithm/pcStable")
public class PcStableController extends AbstractAlgorithmController implements ViewPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(PcStableController.class);

    private final String pcStable;

    private final AlgorithmService algorithmService;

    @Autowired(required = true)
    public PcStableController(
            @Value("${ccd.algorithm.pcstable:edu.pitt.dbmi.ccd.algorithm.tetrad.PcStableApp}") String pcStable,
            AlgorithmService algorithmService,
            @Value("${ccd.algorithm.jar:ccd-algorithm.jar}") String algorithmJar,
            VariableTypeService variableTypeService,
            FileDelimiterService fileDelimiterService,
            DataFileService dataFileService,
            DataFileInfoService dataFileInfoService,
            DataService dataService) {
        super(algorithmJar, variableTypeService, fileDelimiterService, dataFileService, dataFileInfoService, dataService);
        this.pcStable = pcStable;
        this.algorithmService = algorithmService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showPcStableView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        PcStableRunInfo info = new PcStableRunInfo();
        info.setAlpha(0.0001D);
        info.setDepth(3);
        info.setVerbose(Boolean.TRUE);
        info.setJvmOptions("");
        info.setRunOnPsc(Boolean.FALSE);

        Map<String, String> map = directoryFileListing(appUser.getDataDirectory(), appUser.getUsername());
        if (map.isEmpty()) {
            info.setDataset("");
        } else {
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                info.setDataset(key);
                break;
            }
        }

        model.addAttribute("datasetList", map);
        model.addAttribute("algoInfo", info);

        return PC_STABLE_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runPcStable(
            @ModelAttribute("algoInfo") final PcStableRunInfo info,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        List<String> commands = new LinkedList<>();
        if (info.getRunOnPsc()) {
            commands.add("--alpha");
            commands.add(String.valueOf(info.getAlpha().doubleValue()));

            commands.add("--depth");
            commands.add(String.valueOf(info.getDepth().intValue()));

            if (info.getVerbose()) {
                commands.add("--verbose");
            }

            algorithmService.runRemotely("pcstable", info.getDataset(), commands, appUser.getUsername());
        } else {
            commands.add("java");

            String jvmOptions = info.getJvmOptions().trim();
            if (jvmOptions.length() > 0) {
                commands.addAll(Arrays.asList(jvmOptions.split("\\s+")));
            }

            Path classPath = Paths.get(appUser.getLibDirectory(), algorithmJar);
            commands.add("-cp");
            commands.add(classPath.toString());
            commands.add(pcStable);

            Path dataset = Paths.get(appUser.getDataDirectory(), info.getDataset());
            commands.add("--data");
            commands.add(dataset.toString());

            commands.add("--delimiter");
            commands.add(getFileDelimiter(appUser.getDataDirectory(), info.getDataset()));

            commands.add("--alpha");
            commands.add(String.valueOf(info.getAlpha().doubleValue()));

            commands.add("--depth");
            commands.add(String.valueOf(info.getDepth().intValue()));

            if (info.getVerbose()) {
                commands.add("--verbose");
            }

            String fileName = String.format("pc-stable_%s_%d", info.getDataset(), System.currentTimeMillis());
            commands.add("--out-filename");
            commands.add(fileName);

            try {
                algorithmService.runAlgorithm(commands, fileName, appUser.getTmpDirectory(), appUser.getResultDirectory());
            } catch (Exception exception) {
                LOGGER.error("Unable to run PC-Stable.", exception);
            }
        }

        model.addAttribute("title", "PC-Stable is Running");

        return ALGO_RUN_CONFIRM_VIEW;
    }

}
