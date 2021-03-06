package com.laioffer.githubexample.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.laioffer.githubexample.R;
import com.laioffer.githubexample.base.BaseFragment;
import com.laioffer.githubexample.databinding.CustomMapInfoWindowBinding;
import com.laioffer.githubexample.databinding.MapFragmentBinding;
import com.laioffer.githubexample.remote.response.Job;
import com.laioffer.githubexample.ui.HomeList.HomeListFragment;
import com.laioffer.githubexample.ui.NavigationManager;

import com.laioffer.githubexample.ui.search.SearchEvent;
import com.laioffer.githubexample.util.Config;

import com.laioffer.githubexample.ui.jobInfo.JobInfoFragment;
import com.laioffer.githubexample.util.Config;

import com.laioffer.githubexample.util.Utils;
import com.squareup.picasso.Picasso;
import com.wanderingcan.persistentsearch.PersistentSearchView;

import java.util.List;
import java.util.Objects;

public class MapFragment extends BaseFragment<MapViewModel, MapRepository>
        implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    private MapFragmentBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private NavigationManager navigationManager;

    public static MapFragment getInstance(String keyword) {
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =  MapFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.event_map_view);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        FloatingActionButton fab = view.findViewById(R.id.fab_return);
        fab.setOnClickListener( v -> navigationManager.navigateTo(new HomeListFragment(new SearchEvent(0,"Developer"))));

        FloatingActionButton returnFab = view.findViewById(R.id.fab_center);
        returnFab.setOnClickListener( v -> {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Config.latitude, Config.longitude))
                    .zoom(10)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });

        viewModel.getListMutableLiveData().observe(getViewLifecycleOwner(), list -> {
            viewModel.getSavedJob().clear();
            if (list != null && !list.isEmpty()) {
                addJobToMap(list);
                viewModel.getSavedJob().addAll(list);
            }
            //this should happen for recommend item.
//
//            CardFragmentPagerAdapter pagerAdapter = new CardFragmentPagerAdapter(getChildFragmentManager(), Utils.dpToPixels(2, getContext()), list);
//            ShadowTransformer shadowTransformer = new ShadowTransformer(binding.mapViewPager, pagerAdapter);
//
//            binding.mapViewPager.setAdapter(pagerAdapter);
//            binding.mapViewPager.setPageMargin(120);
//            binding.mapViewPager.setPageTransformer(false ,shadowTransformer);
//            binding.mapViewPager.setOffscreenPageLimit(3);
//            shadowTransformer.enableScaling(true);

        });

        viewModel.getRecommendation().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.size() == 0) {
                Utils.constructToast(getContext(), "No recommendations. Please save job frist").show();
                return;
            }
//            //this should happen for recommend item.
//
            CardFragmentPagerAdapter pagerAdapter = new CardFragmentPagerAdapter(getChildFragmentManager(), Utils.dpToPixels(2, getContext()), list);
            ShadowTransformer shadowTransformer = new ShadowTransformer(binding.mapViewPager, pagerAdapter);

            binding.mapViewPager.setAdapter(pagerAdapter);
            binding.mapViewPager.setPageMargin(120);
            binding.mapViewPager.setPageTransformer(false ,shadowTransformer);
            binding.mapViewPager.setOffscreenPageLimit(3);
            shadowTransformer.enableScaling(true);
        });

        viewModel.getMsg().observe(getViewLifecycleOwner(), msg ->
                Utils.constructToast(getContext(), msg).show());

        binding.searchBar.setOnSearchListener(new PersistentSearchView.OnSearchListener() {
            @Override
            public void onSearchOpened() {
                binding.searchBar.getSearchMenu().addSearchMenuItem(0, "All");
                binding.searchBar.getSearchMenu().addSearchMenuItem(1, "Past 1 days");
                binding.searchBar.getSearchMenu().addSearchMenuItem(2, "Past 3 days");
                binding.searchBar.getSearchMenu().addSearchMenuItem(3, "Past 7 day");
            }

            @Override
            public void onSearchClosed() {
                binding.searchBar.getSearchMenu().clearItems();
            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchTermChanged(CharSequence term) {

            }


            @Override
            public void onSearch(CharSequence text) {
                googleMap.clear();
                setMarkerAtCurrentPosition();
                viewModel.setSearchEvent(text.toString());
            }
        });


        binding.searchBar.setOnMenuItemClickListener(item -> {
            Utils.constructToast(getContext(), item.getTitle()).show();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navigationManager = (NavigationManager) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(Objects.requireNonNull(getContext()));
        this.googleMap = googleMap;
        this.googleMap.setMapStyle(MapStyleOptions
                .loadRawResourceStyle(Objects.requireNonNull(getActivity()), R.raw.style_json));
        setMarkerAtCurrentPosition();
        googleMap.setInfoWindowAdapter(this);
        googleMap.setOnInfoWindowClickListener(this);
        if (viewModel.getSavedJob().isEmpty()) {
            Bundle args = getArguments();
            if (args == null || Utils.isNullOrEmpty(args.getString("keyword"))) {
                viewModel.setSearchEvent("");
            } else {
                viewModel.setSearchEvent(args.getString("keyword"));
            }
        } else {
            addJobToMap(viewModel.getSavedJob());
        }
    }

    @Override
    protected MapViewModel getViewModel() {
        return new ViewModelProvider(requireActivity(), getFactory()).get(MapViewModel.class);
    }

    @Override
    protected ViewModelProvider.Factory getFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MapViewModel(getRepository());
            }
        };
    }

    @Override
    protected MapRepository getRepository() {
        return new MapRepository();
    }

    private void setMarkerAtCurrentPosition() {
        if (googleMap == null) {
            return;
        }
        LatLng position = new LatLng(Config.latitude, Config.longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(10)
                .build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        MarkerOptions markerOptions = new MarkerOptions().position(position).title("Me");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        googleMap.addMarker(markerOptions);
        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });
    }

    private void addJobToMap(List<Job> jobs) {
        if (googleMap == null) {
            return;
        }
        if (jobs == null) {
            Utils.constructToast(getContext(), "Null List!").show();
            return;
        }

        for (Job job : jobs) {
            LatLng position = new LatLng(job.location.latitude, job.location.longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            googleMap.addMarker(markerOptions);
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(job);
        }

    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (!(marker.getTag() instanceof Job)) {
            return null;
        }
        Job currJob = (Job) marker.getTag();
        if (currJob == null) {
            return null;
        }
        CustomMapInfoWindowBinding binding = CustomMapInfoWindowBinding.inflate(getLayoutInflater());
        binding.tvTitle.setText(currJob.name);
        binding.tvCompanyName.setText(currJob.company);
        binding.tvLocation.setText(currJob.address);
        if (!currJob.imageUrl.isEmpty()) {
            Picasso.get().setLoggingEnabled(true);
            Picasso.get().load(currJob.imageUrl).placeholder(R.drawable.thumbnail)
                    .resize(70,70)
                    .into(binding.imgInfo);

        }
        return binding.getRoot();
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!(marker.getTag() instanceof Job)) {
            return;
        }
        Job currJob = (Job) marker.getTag();
        if (currJob == null) {
            return;
        }
        JobInfoFragment fragment = JobInfoFragment.newInstance(currJob);
        navigationManager.navigateTo(fragment);
    }

}


///////