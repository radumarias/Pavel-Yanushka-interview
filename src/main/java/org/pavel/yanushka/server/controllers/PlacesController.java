package org.pavel.yanushka.server.controllers;

import org.pavel.yanushka.common.models.CitySuggests;
import org.pavel.yanushka.common.models.Place;
import org.pavel.yanushka.server.services.PlacesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest")
public class PlacesController {

    private static final Logger logger = LoggerFactory.getLogger(PlacesController.class);

    private final PlacesService placesService;

    public PlacesController(PlacesService placesService) {
        this.placesService = placesService;
    }

    @GetMapping(value = "/places/{placeId}")
    @ResponseStatus(HttpStatus.OK)
    public Place getPlaces(@PathVariable String placeId) {
        logger.debug("Going to process get request for places with placeId {}", placeId);
        return placesService.getPlacesForCity(placeId);
    }

    @GetMapping(value = "/suggest/{query}")
    @ResponseStatus(HttpStatus.OK)
    public CitySuggests getSuggests(@PathVariable String query) {
        logger.debug("Going to process get request for suggest with query {} ", query);
        return placesService.getCitySuggests(query);
    }
}
