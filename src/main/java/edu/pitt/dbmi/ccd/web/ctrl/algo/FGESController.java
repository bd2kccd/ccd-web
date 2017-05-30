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

import edu.pitt.dbmi.ccd.web.ctrl.ViewPath;
import edu.pitt.dbmi.ccd.web.model.AppUser;
import edu.pitt.dbmi.ccd.web.model.algo.AlgorithmJobRequest;
import edu.pitt.dbmi.ccd.web.model.algo.FGEScAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.FGESdAlgoOpt;
import edu.pitt.dbmi.ccd.web.model.algo.FGESmCGAlgoOpt;
import edu.pitt.dbmi.ccd.web.prop.CcdProperties;
import edu.pitt.dbmi.ccd.web.service.algo.AlgorithmService;
import edu.pitt.dbmi.ccd.web.util.TetradCmdOptions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 *
 * Nov 17, 2015 11:42:14 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Controller
@SessionAttributes("appUser")
@RequestMapping(value = "algorithm/fges")
public class FGESController extends AbstractTetradAlgoController implements ViewPath, TetradCmdOptions {

    private final AlgorithmService algorithmService;
    private final CcdProperties ccdProperties;

    @Autowired
    public FGESController(AlgorithmService algorithmService, CcdProperties ccdProperties) {
        this.algorithmService = algorithmService;
        this.ccdProperties = ccdProperties;
    }

    @RequestMapping(value = "fgesm_cg", method = RequestMethod.POST)
    public String runshowFgesmCG(
            @ModelAttribute("algoOpt") final FGESmCGAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESm-CG", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFgesmCG());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForMixedCG(algoOpt, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesm_cg", method = RequestMethod.GET)
    public String showFgesmCGView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserMixedDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGESmCGAlgoOpt algoOpt = new FGESmCGAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return FGES_MIXED_CG_VIEW;
    }

    @RequestMapping(value = "fgesd", method = RequestMethod.POST)
    public String runFgesDiscrete(
            @ModelAttribute("algoOpt") final FGESdAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESd", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFgesDisc());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForDiscrete(algoOpt, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesd", method = RequestMethod.GET)
    public String showFgesDiscreteView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserDiscreteDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGESdAlgoOpt algoOpt = new FGESdAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return FGES_DISC_VIEW;
    }

    @RequestMapping(value = "fgesc", method = RequestMethod.POST)
    public String runFgesContinuous(
            @ModelAttribute("algoOpt") final FGEScAlgoOpt algoOpt,
            @ModelAttribute("appUser") final AppUser appUser,
            final Model model) {
        AlgorithmJobRequest jobRequest = new AlgorithmJobRequest("FGESc", ccdProperties.getAlgoJar(), ccdProperties.getAlgoFgesCont());
        jobRequest.setDataset(getDataset(algoOpt));
        jobRequest.setPriorKnowledge(getPriorKnowledge(algoOpt));
        jobRequest.setJvmOptions(getJvmOptions(algoOpt));
        jobRequest.setParameters(getParametersForContinuous(algoOpt, appUser.getUsername()));

        algorithmService.addToQueue(jobRequest, appUser.getUsername());

        return REDIRECT_JOB_QUEUE;
    }

    @RequestMapping(value = "fgesc", method = RequestMethod.GET)
    public String showFgesContinuousView(@ModelAttribute("appUser") final AppUser appUser, final Model model) {
        Map<String, String> dataset = algorithmService.getUserContinuousDataset(appUser.getUsername());
        Map<String, String> prior = algorithmService.getUserPriorKnowledgeFiles(appUser.getUsername());
        FGEScAlgoOpt algoOpt = new FGEScAlgoOpt();

        // set the default dataset
        if (!dataset.isEmpty()) {
            algoOpt.setDataset(dataset.keySet().iterator().next());  // get one element
        }

        if (!prior.isEmpty()) {
            algoOpt.setPriorKnowledge(prior.keySet().iterator().next());
        }

        model.addAttribute("datasetList", dataset);
        model.addAttribute("priorList", prior);
        model.addAttribute("algoOpt", algoOpt);

        return FGES_CONT_VIEW;
    }

    private List<String> getParametersForMixedCG(FGESmCGAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(NUM_CATEGORIES_TO_DISCRETIZE);
        parameters.add(Integer.toString(algoOpt.getNumCategoriesToDiscretize()));
        parameters.add(NUM_DISCRETE_CATEGORIES);
        parameters.add(Integer.toString(algoOpt.getNumberOfDiscreteCategories()));
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        if (algoOpt.isDiscretize()) {
            parameters.add(DISCRETIZE);
        }
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isSymmetricFirstStep()) {
            parameters.add(SYMMETRIC_FIRST_STEP);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

    private List<String> getParametersForDiscrete(FGESdAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(SAMPLE_PRIOR);
        parameters.add(Double.toString(algoOpt.getSamplePrior()));
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isSymmetricFirstStep()) {
            parameters.add(SYMMETRIC_FIRST_STEP);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipCategoryLimit()) {
            parameters.add(SKIP_CATEGORY_LIMIT);
        }
        if (algoOpt.isSkipUniqueVarName()) {
            parameters.add(SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

    private List<String> getParametersForContinuous(FGEScAlgoOpt algoOpt, String username) {
        List<String> parameters = new LinkedList<>();
        String delimiter = algorithmService.getFileDelimiter(algoOpt.getDataset(), username);
        parameters.add(DELIMITER);
        parameters.add(delimiter);
        parameters.add(PENALTY_DISCOUNT);
        parameters.add(Double.toString(algoOpt.getPenaltyDiscount()));
        parameters.add(STRUCTURE_PRIOR);
        parameters.add(Double.toString(algoOpt.getStructurePrior()));
        parameters.add(MAX_DEGREE);
        parameters.add(Integer.toString(algoOpt.getMaxDegree()));
        if (algoOpt.isFaithfulnessAssumed()) {
            parameters.add(FAITHFULNESS_ASSUMED);
        }
        if (algoOpt.isSymmetricFirstStep()) {
            parameters.add(SYMMETRIC_FIRST_STEP);
        }
        if (algoOpt.isVerbose()) {
            parameters.add(VERBOSE);
        }
        if (algoOpt.isSkipNonZeroVariance()) {
            parameters.add(SKIP_NONZERO_VARIANCE);
        }
        if (algoOpt.isSkipUniqueVarName()) {
            parameters.add(SKIP_UNIQUE_VAR_NAME);
        }

        parameters.add(TETRAD_GRAPH_JSON);

        return parameters;
    }

}
