package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.api.service.AstreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller is purely for mass updating data when changing/updating the data structure
 */
@RestController
@SecurityRequirement(name = "BearerAuthentication")
@RequestMapping("/api/rectifier")
public class RectifierController {

    @Autowired
    AstreService astreService;

    @Autowired
    private AstreRepository astreRepository; // Bad practice but this controller isn't meant to be user-reachable

    @GetMapping(path = "/newParentStruct")
    public void newParentStruct(HttpServletRequest request, HttpServletResponse response) {

        List<Astre> astres = astreService.getAllAstre();
        List<Astre> astresCorrected = Collections.synchronizedList(new ArrayList<>());

        astres.parallelStream().filter(astre -> astre.getAstreID().getType().equalsIgnoreCase("topic")).forEach((astre) -> {
            List<Astre> astreFinds = astreRepository.findByName(astre.getParent());
            if (astreFinds.size() == 1) {
                AstreID correctParentAstreID = astreFinds.getFirst().getAstreID();
                //System.out.println("Found unique parent id for " + astre.getAstreID() + " \nFound these " + astreFinds.stream().map(Astre::getAstreID).map(AstreID::toString).collect(Collectors.joining("|")));
                astre.setParentAstreID(correctParentAstreID);
                astresCorrected.add(astre);
            } else {
                System.out.println("Couldn't find unique parent id for " + astre.getAstreID() + " \n>Found these :" + astreFinds.stream().map(Astre::getAstreID).map(AstreID::toString).collect(Collectors.joining("|")));
            }
        });
        astreRepository.saveAllAndFlush(astresCorrected);
        return;
    }
}
