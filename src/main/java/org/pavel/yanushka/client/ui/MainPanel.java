package org.pavel.yanushka.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.pavel.yanushka.client.event.GetPlacesEvent;
import org.pavel.yanushka.client.model.ModelHandler;
import org.pavel.yanushka.client.resource.ApplicationResources;
import org.pavel.yanushka.client.rest.RequestBuilderService;
import org.pavel.yanushka.client.ui.component.CitySuggestOracle;
import org.pavel.yanushka.common.models.Candidate;
import org.pavel.yanushka.common.models.City;
import org.pavel.yanushka.common.models.CitySuggests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPanel extends Composite {

    private static final MainPanelUiBinder uiBinder = GWT.create(MainPanelUiBinder.class);

    interface MainPanelUiBinder extends UiBinder<Widget, MainPanel> {
    }

    @UiField
    FlowPanel citiesPanel;

    @UiField
    FlowPanel placesPanel;

    private final ApplicationResources resources;
    private final SuggestBox citySuggest;

    private final Map<Candidate, PlacesWidget> placeWidgets;
    private final SimpleEventBus eventBus;
    private final ModelHandler modelHandler;

    @Inject
    public MainPanel(SimpleEventBus eventBus, ModelHandler modelHandler, RequestBuilderService requestBuilderService,
                     ApplicationResources resources) {
        this.eventBus = eventBus;
        this.resources = resources;
        initWidget(uiBinder.createAndBindUi(this));
        placeWidgets = new HashMap<>();
        this.modelHandler = modelHandler;
        final CitySuggestOracle citySuggestOracle = new CitySuggestOracle(requestBuilderService);
        citySuggest = new SuggestBox(citySuggestOracle);

        final SelectionHandler<SuggestOracle.Suggestion> suggestionSelectionHandler = event -> {
            final String selectedCityName = event.getSelectedItem().getDisplayString();
            if (selectedCityName != null && !selectedCityName.isEmpty()) {
                final CitySuggests latestSuggestions =
                        ((CitySuggestOracle) citySuggest.getSuggestOracle()).getLatestSuggestions();
                latestSuggestions.getSuggestsList().stream()
                        .filter(city -> selectedCityName.equals(city.getName()))
                        .findFirst()
                        .ifPresent(this::fireGetPlacesEvent);
            }
        };
        citySuggest.addSelectionHandler(suggestionSelectionHandler);
        placesPanel.add(citySuggest);
    }

    public void addPlaceToPanel(Candidate candidate) {
        PlacesWidget placeWidget = new PlacesWidget(candidate, eventBus);
        citiesPanel.add(placeWidget);
        placeWidgets.putIfAbsent(candidate, placeWidget);
    }

    public void removeAllPlaces() {
        citiesPanel.clear();
        placeWidgets.clear();
    }

    public void showPlaces() {
        removeAllPlaces();
        List<Candidate> all = modelHandler.getAll();
        if (!all.isEmpty()) {
            all.forEach(this::addPlaceToPanel);
        }
    }

    private void fireGetPlacesEvent(City city) {
        startLoading();
        eventBus.fireEvent(new GetPlacesEvent(city.getPlaceId()));
    }

    private void startLoading() {
        removeAllPlaces();
        Image loadingImage = new Image(resources.loading());
        loadingImage.setStyleName(resources.style().center());
        citiesPanel.add(loadingImage);
    }
}
