package com.example.ditugaode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.ditugaode.adapter.BusResultListAdapter;
import com.example.ditugaode.adapter.DrivePathAdapter;
import com.example.ditugaode.adapter.RideStepAdapter;
import com.example.ditugaode.adapter.WalkStepAdapter;
import com.example.ditugaode.behavior.NoAnchorBottomSheetBehavior;
import com.example.ditugaode.overlay.AMapUtil;
import com.example.ditugaode.overlay.BusRouteOverlay;
import com.example.ditugaode.overlay.DrivingRouteOverlay;
import com.example.ditugaode.overlay.RideRouteOverlay;
import com.example.ditugaode.overlay.WalkRouteOverlay;
import com.example.ditugaode.pickpoi.PoiItemEvent;
import com.example.ditugaode.pickpoi.PoiSearchActivity2;
import com.example.ditugaode.pickpoi.SelectedMyPoiEvent;
import com.example.ditugaode.view.base.InputMethodUtils;
import com.example.ditugaode.view.base.MapViewInterface;
import com.example.ditugaode.view.base.MyAMapUtils;
import com.example.ditugaode.view.base.OnItemClickListener;
import com.example.ditugaode.view.base.PoiSearchActivity;
import com.example.ditugaode.view.base.WalkRouteNaviAcitivity;
import com.example.ditugaode.view.map.AMapServicesUtil;
import com.example.ditugaode.view.map.GPSView;
import com.example.ditugaode.view.map.MapHeaderView;
import com.example.ditugaode.view.map.PoiDetailBottom;
import com.example.ditugaode.view.map.SupendViewContainer;
import com.example.ditugaode.view.map.TrafficView;
import com.example.ditugaode.view.map.ViewAnimUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements LocationSource, TextWatcher,AMapLocationListener, Inputtips.InputtipsListener, TrafficView.OnTrafficChangeListener, AMap.OnMapTouchListener, MapViewInterface,AMap.OnPOIClickListener,GPSView.OnGPSViewClickListener,View.OnClickListener, AMapGestureListener,MapHeaderView.OnMapHeaderViewClickListener,PoiDetailBottom.OnPoiDetailBottomClickListener,OnItemClickListener,RouteSearch.OnRouteSearchListener,BusResultListAdapter.BusListItemListner{
    public static final String CITY_CODE="CityCode";

    private MapView mapView = null;
    private AMap aMap;
    private AMapLocation aMapLocation;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private UiSettings uiSettings;
    private OnLocationChangedListener mListener;
    private TrafficView mTrafficView;
    boolean useMoveToLocationWithMapMode = true;
    private int mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;//??????????????????
    private float mAccuracy;
    private GPSView gpsView;
    private int mZoomLevel = 16;//??????????????????????????????????????????20
    private static long mAnimDuartion = 500L;//??????????????????
    private LatLng mLatLng;//?????????????????????
    private LatLng mClickPointLatLng;//???????????????poi?????????
    private static boolean mFirstLocation = true;//???????????????
    private int mCurrentGpsState = STATE_UNLOCKED;//??????????????????
    private static final int STATE_UNLOCKED = 0;//??????????????????????????????
    private static final int STATE_LOCKED = 1;//????????????
    private static final int STATE_ROTATE = 2;//??????????????????????????????
    private SensorEventHelper mSensorHelper;
    //???????????????????????????Marker
    private Marker mLocMarker;//??????????????????
    private boolean mMoveToCenter = true;//????????????????????????????????????
    private Circle mCircle;
    private MyLocationStyle mLocationStyle;
    // ????????????????????????POI??????
    private boolean isPoiClick;
    private TextView mTvLocation;
    private String mPoiName;
    private LinearLayout mLLSearchContainer;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private MapHeaderView mMapHeaderView;
    private View mBottomSheet;
    private BottomSheetBehavior<View> mBehavior;
    private int mMaxPeekHeight;//????????????
    private int mMinPeekHeight;//????????????
    private int mScreenHeight;
    private int mScreenWidth;
    private TextView mTvLocTitle;
    private View mPoiColseView;
    private boolean slideDown;//????????????
    private int moveY;
    private int[] mBottomSheetLoc = new int[2];
    private int mPadding;
    private View mGspContainer;
    private boolean onScrolling;//??????????????????
    private PoiDetailBottom mPoiDetailTaxi;
    private TextView mTvRoute;
    private TextView mTvCall;

    private String mCity;
    private EditText mEtSearchTip;
    private ImageView mIvLeftSearch;
    private SupendViewContainer mSupendPartitionView;
    private LocationManager mLocMgr;
    private RecyclerView mRecycleViewSearch;
    private ProgressBar mSearchProgressBar;
    private SearchAdapter mSearchAdapter;
    // ??????????????????
    private List<Tip> mSearchData = new ArrayList<>();

    private SupendViewContainer supendViewContainer;
    private TextView mtvGo;
    private WalkStepAdapter mWalkStepAdapter;
    private DrivePathAdapter mDrivePathAdapter;
    private RideStepAdapter mRideStepAdapter;
    private BusResultListAdapter mBusResultAdapter;

    private WalkRouteResult mWalkRouteResult;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private RideRouteResult mRideRouteResult;

    private Context mContext;
    private RecyclerView mPathDetailRecView;
    private LinearLayout mPathLayout;
    private LinearLayout mPathLayout1;
    private LinearLayout mPathLayout2;
    private TextView mPathDurText;
    private TextView mPathDurText1;
    private TextView mPathDurText2;
    private TextView mPathDisText;
    private TextView mPathDisText1;
    private TextView mPathDisText2;
    private LinearLayout mSheetHeadLayout;
    private int mTopLayoutHeight=200;
    private RelativeLayout mTopLayout;
    private NoAnchorBottomSheetBehavior mBehavior2;
    private NestedScrollView mNesteScrollView;
    private TextView mNaviText;
    private Button mNaviBtn;
    private TextView mFloatBtn;
    private Poi mEndPoi;
    private Poi mStartPoi;
//    private int mSelectedType =TYPE_DRIVE;
    private static final int MSG_MOVE_CAMERA = 0x01;
    private final int TYPE_DRIVE=100;
    private final int TYPE_BUS=101;
    private final int TYPE_WALK=102;
    private final int TYPE_RIDE=103;
    private int mSelectedType =TYPE_DRIVE;

    private ConstraintLayout constraintLayoutAll;
    private RecyclerView mBusResultRview;
    private ImageView back;
    private TabLayout mTabLayout;
    private TextView mPathTipsText;
    private TextView mFromText;
    private TextView mTargetText;
    private Button startbtn;
    private TextView startbtn1;
    private ImageView change;
    private LinearLayout pathlayout;
    private LinearLayout pathlayout1;
    private LinearLayout pathlayout2;
    private Location mLocation;

    private boolean isTvGo;
    private TextView mPoiTitleText;
    private TextView mPoiDescText;
    private ImageView mgo;
    private ConstraintLayout mFrequentView;


    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the view_supend_container for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated (@Nullable Bundle savedInstanceState) {
        Log.d("create","onActivityCreated???????????????");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onActivityCreated(savedInstanceState);

        EventBus.getDefault().register(this);//??????

        //??????????????????
        View decorview = requireActivity().getWindow().getDecorView();
        decorview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        setAndroidNativeLightStatusBar(getActivity(),true);//????????????????????????????????????

        mContext = getActivity();
        supendViewContainer = requireActivity().findViewById(R.id.supend);
        mapView = requireActivity().findViewById(R.id.map);
        gpsView =  requireActivity().findViewById(R.id.gpsView);
        gpsView.setGpsState(mCurrentGpsState);
        mapView.onCreate(savedInstanceState);
        mTrafficView = requireActivity().findViewById(R.id.tv_toast);
        mMapHeaderView = (MapHeaderView)requireActivity().findViewById(R.id.mhv);
        mTvLocation = requireActivity().findViewById(R.id.tv_my_loc);
        mLLSearchContainer = requireActivity().findViewById(R.id.ll_search_container);
        mLLSearchContainer.getBackground().setAlpha(0);
        //????????????BottomSheet
        mBottomSheet =requireActivity().findViewById(R.id.poi_detail_bottom);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheet.setVisibility(View.GONE);
        mTvLocTitle = requireActivity().findViewById(R.id.tv_title);
        mPoiColseView = requireActivity().findViewById(R.id.iv_close);
        mPadding = getResources().getDimensionPixelSize(R.dimen.padding_size);
        mGspContainer = requireActivity().findViewById(R.id.all_gps);
        mPoiDetailTaxi =requireActivity().findViewById(R.id.poi_detail_taxi);
        mPoiDetailTaxi.clearAnimation();
        mPoiDetailTaxi.setVisibility(View.GONE);
        mTvRoute = requireActivity().findViewById(R.id.tv_route);
        mIvLeftSearch = requireActivity().findViewById(R.id.iv_search_left);
        mEtSearchTip = requireActivity().findViewById(R.id.et_search_tip);
        mRecycleViewSearch = requireView().findViewById(R.id.rv_search);
        mRecycleViewSearch.getBackground().setAlpha(230);
        mIvLeftSearch = (ImageView)requireActivity().findViewById(R.id.iv_search_left);
        mEtSearchTip = (EditText)requireActivity().findViewById(R.id.et_search_tip);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecycleViewSearch.setLayoutManager(layoutManager);
        mSearchProgressBar = (ProgressBar)requireActivity().findViewById(R.id.progressBar);
        mTvCall = requireActivity().findViewById(R.id.tv_call_taxi);
        mtvGo = requireActivity().findViewById(R.id.tv_go);
        mPathDetailRecView = requireActivity().findViewById(R.id.path_detail_recyclerView);
        constraintLayoutAll = requireActivity().findViewById(R.id.constraintLayout_all);
        back = requireActivity().findViewById(R.id.back);

        mPathLayout = requireActivity().findViewById(R.id.path_layout);
        mPathLayout1 = requireActivity().findViewById(R.id.path_layout1);
        mPathLayout2 = requireActivity().findViewById(R.id.path_layout2);
        mPathDurText = requireActivity().findViewById(R.id.path_general_time);
        mPathDurText1 = requireActivity().findViewById(R.id.path_general_time1);
        mPathDurText2 = requireActivity().findViewById(R.id.path_general_time2);
        mPathDisText = requireActivity().findViewById(R.id.path_general_distance);
        mPathDisText2 = requireActivity().findViewById(R.id.path_general_distance1);
        mPathDisText1 = requireActivity().findViewById(R.id.path_general_distance2);
        mSheetHeadLayout= requireActivity().findViewById(R.id.sheet_head_layout);
        mTopLayout = requireActivity().findViewById(R.id.topLayout);
        mTopLayout.setVisibility(View.GONE);
        mNesteScrollView = requireActivity().findViewById(R.id.bottom_sheet);

        mNesteScrollView.setVisibility(View.GONE);

        mNaviText = requireActivity().findViewById(R.id.navi_start_btn_1);
        mNaviBtn = requireActivity().findViewById(R.id.navi_start_btn);
        mFloatBtn = requireActivity().findViewById(R.id.tv_go);
        mBusResultRview = requireView().findViewById(R.id.bus_result_recyclerView);

        mTabLayout = requireActivity().findViewById(R.id.route_plan_tab_layout);
        mPathTipsText = requireActivity().findViewById(R.id.path_detail_traffic_light_text);
        mFromText = requireActivity().findViewById(R.id.route_plan_start_edit_layout);
        mTargetText = requireActivity().findViewById(R.id.route_plan_to_edit_layout);
        startbtn = requireActivity().findViewById(R.id.navi_start_btn);
        startbtn1 = requireActivity().findViewById(R.id.navi_start_btn_1);
        change = requireActivity().findViewById(R.id.route_plan_exchange_btn);
        pathlayout = requireActivity().findViewById(R.id.path_layout);
        pathlayout1 = requireActivity().findViewById(R.id.path_layout1);
        pathlayout2 = requireActivity().findViewById(R.id.path_layout2);
        mPoiTitleText = requireActivity().findViewById(R.id.route_plan_poi_title);
        mPoiDescText = requireActivity().findViewById(R.id.route_plan_poi_desc);
        mgo = requireActivity().findViewById(R.id.go);
        mFrequentView = requireActivity().findViewById(R.id.fv);
        initTabLayout();
        //?????????
        if(aMap == null) {
            Log.d("create","???????????????");
            aMap = mapView.getMap();
            aMap.showIndoorMap(true);
            uiSettings = aMap.getUiSettings();
//            uiSettings.setCompassEnabled(true); //???????????????
            uiSettings.setRotateGesturesEnabled(false);

            aMap.setLocationSource(this);//??????????????????
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.setMyLocationEnabled(true);//?????????????????????????????????

            aMap.setTrafficEnabled(true);//??????????????????
            setBottomSheet();

            setListener();
            setLocationStyle();
        }
    }

    /**
     * ???????????? ??????
     */
    private void setListener(){
        gpsView.setOnGPSViewClickListener(this);
        mSensorHelper = new SensorEventHelper(getActivity());
        //??????????????????
        aMap.setAMapGestureListener(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        if(mTrafficView != null){
            mTrafficView.setOnTrafficChangeListener(this);
        }
        if(null != mPoiColseView){
            mPoiColseView.setOnClickListener(this);
        }
        mMapHeaderView.setOnMapHeaderViewClickListener(this);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            private float lastSlide;//??????slideOffset
            private float currSlide;//??????slideOffset

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i){
                    //??????
                    case BottomSheetBehavior.STATE_EXPANDED:
                       Log.d("be","??????");
                        smoothSlideUpMap();
                        break;
                    //??????
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("be","??????");
                        onPoiDetailCollapsed();
                        slideDown = false;
                        break;
                    //??????
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //??????
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //???????????????
                        break;
                }
            }
            /**
             * BottomSheet????????????
             */

            @Override
            public void onSlide(@NonNull View view, float v) {
                currSlide = v;
//                Log.d("upBottom","????????????");
                if(v > 0){
                    mPoiColseView.setVisibility(View.GONE);
                    showBackToMapState();
                    if(v < 1){

                    }
                    mMoveToCenter =false;
                    if (currSlide - lastSlide > 0){
                        Log.i("slide",">>>>>????????????");
                        slideDown = false;
                        onPoiDetailExpanded();
//                        smoothSlideUpMap();
                    }else if(currSlide - lastSlide < 0){
                        Log.i("slide",">>>>>????????????");
                        if(!slideDown){
                            smoothSlideDownMap();
                        }
                    }
                }else if(v == 0){
                    //?????????COLLAPSED??????
                    mPoiColseView.setVisibility(View.VISIBLE);
                    showPoiDetailState();
                }else if (v < 0) {
                    //???COLLAPSED???HIDDEN???????????????????????????BottomSheet??????
                    //setHideable(false)??????Behavior?????????????????????????????????????????????
                    mBehavior.setHideable(false);
                }
                lastSlide = currSlide;
            }
        });
        mPoiDetailTaxi.setOnPoiDetailBottomClickListener(this);
        aMap.setOnPOIClickListener(this);
        mTvRoute.setOnClickListener(this);
        mTvCall.setOnClickListener(this);
        mtvGo.setOnClickListener(this);
        mIvLeftSearch.setOnClickListener(this);
        // ????????????????????????????????????
        mIvLeftSearch.setOnClickListener(this);
        back.setOnClickListener(this);
        // ????????????RecyclerView
        mSearchAdapter = new SearchAdapter(mSearchData);
        mRecycleViewSearch.setAdapter(mSearchAdapter);
        // ???????????????
        mEtSearchTip.addTextChangedListener(this);
        mSearchAdapter.setOnItemClickListener(this);


     mTopLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
         @Override
         public void onGlobalLayout() {
             mTopLayoutHeight = mTopLayout.getHeight();
             mTopLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
         }
     });
        mBehavior2 = NoAnchorBottomSheetBehavior.from(mNesteScrollView);
        mBehavior2.setState(NoAnchorBottomSheetBehavior.STATE_COLLAPSED);
        mBehavior2.setPeekHeight(getSheetHeadHeight());
        mBehavior2.setBottomSheetCallback(new NoAnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (mTopLayout.getVisibility()==View.VISIBLE && mPathDetailRecView.getVisibility()==View.VISIBLE){
                    if (slideOffset > 0.5){
                        mNaviText.setVisibility(View.GONE);
                        mNaviBtn.setVisibility(View.VISIBLE);
                    }else {
                        mNaviText.setVisibility(View.VISIBLE);
                        mNaviBtn.setVisibility(View.GONE);
                    }
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPathDetailRecView.setLayoutManager(linearLayoutManager);
        mFromText.setOnClickListener(this);
        mTargetText.setOnClickListener(this);
        change.setOnClickListener(this);
        pathlayout.setOnClickListener(this);
        pathlayout1.setOnClickListener(this);
        pathlayout2.setOnClickListener(this);
        startbtn.setOnClickListener(this);
        startbtn1.setOnClickListener(this);
        mgo.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getActivity());
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        mBusResultRview.setLayoutManager(linearLayoutManager2);
        mFrequentView.setOnClickListener(this);
    }
    /**
     * ???GpsButton?????????poi detail??????
     */
    private void moveGspButtonAbove() {

        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (gpsView.isAbovePoiDetail()) {
                    //???????????????????????????????????????
                    return;
                }
                if (moveY == 0) {
                    //??????Y?????????????????????
                    moveY = mGspContainer.getTop() - mBottomSheet.getTop() + mGspContainer.getMeasuredHeight() + mPadding;
                    mBottomSheet.getLocationInWindow(mBottomSheetLoc);
                }
                if (moveY > 0) {
                    mGspContainer.setTranslationY(-moveY);
                    gpsView.setAbovePoiDetail(true);
                }
            }
        });



    }
    /**
     * ???GpsButton?????????????????????
     */
    private void resetGpsButtonPosition() {

        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (!gpsView.isAbovePoiDetail()) {
                    //???????????????????????????????????????
                    return;
                }
                //??????????????????
                mGspContainer.setTranslationY(0);
                gpsView.setAbovePoiDetail(false);
            }
        });

    }


    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        /*if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }

    private void addRotateMarker(LatLng latlng) {
       /* if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        //3D??????
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_3d)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d("zh","onLocationChanged?????????");
        if(mListener != null && aMapLocation != null){
            if(aMapLocation.getErrorCode() == 0){
                Log.d("zh","???????????????");
                Log.d("zh", String.valueOf(mFirstLocation));
                this.aMapLocation = aMapLocation;
                mLocation = aMapLocation;

               mLatLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                if(aMapLocation.getPoiName() != null && !aMapLocation.getPoiName().equals(mPoiName)){
                    if(!isPoiClick){
                        // ??????poi???,??????????????????????????????????????????
                        mPoiName = aMapLocation.getPoiName();
                        showPoiNameText(String.format("???%s??????", mPoiName));
                    }
                }
                if(!aMapLocation.getCity().equals(mCity)){
                    mCity = aMapLocation.getCity();

                }
                //??????????????????????????????
                //??????????????????????????????????????????????????????
                mAccuracy = aMapLocation.getAccuracy();
                if(mFirstLocation){
                    mStartPoi = new Poi(getString(R.string.poi_search_my_location),mLatLng,"");
                    Log.d("zh","????????????");
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel), new AMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            mCurrentGpsState = STATE_LOCKED;
                            gpsView.setGpsState(mCurrentGpsState);
                            mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                            addCircle(mLatLng, mAccuracy);//?????????????????????
                            addMarker(mLatLng);//??????????????????
                            mSensorHelper.setCurrentMarker(mLocMarker);//??????????????????
                            mFirstLocation = false;
                        }

                        @Override
                        public void onCancel() {

                        }

                    });

                }else{
                    //BottomSheet????????????,??????????????????
                    mCircle.setCenter(mLatLng);
                    mCircle.setRadius(mAccuracy);
                    mLocMarker.setPosition(mLatLng);
                    if (mMoveToCenter) {
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, mZoomLevel));
                    }
                }

            }else{
                String errText = "????????????," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }



    /**
     * ????????????
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            Log.d("zh","????????????");
            mLocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            //??????????????????
            mLocationClient.setLocationListener(this);
            //??????????????????????????????
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //??????????????????
            mLocationOption.setInterval(2000);
            //??????????????????
            mLocationOption.setLocationCacheEnable(true);//??????????????????
            mLocationClient.setLocationOption(mLocationOption);
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????2000ms?????????????????????????????????stopLocation()???????????????????????????
            // ???????????????????????????????????????????????????onDestroy()??????
            // ?????????????????????????????????????????????????????????????????????stopLocation()???????????????????????????sdk???????????????
            if (null != mLocationClient) {
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
            }
        }

    }
    /**
     * ????????????
     */
    @Override
    public void deactivate() {
        Log.d("zh","????????????");
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocMarker = null;
        mLocationClient = null;
    }

    /**
     * ????????????POI??????BottomSheet
     */
    private void setBottomSheet(){
        mMinPeekHeight = mBehavior.getPeekHeight();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        if (null == wm) {
            Log.d("zh","??????WindowManager??????");
            return;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        //????????????3/5
        mScreenHeight = point.y;
        mScreenWidth = point.x;
        //??????bottomsheet??????????????? 3/5
        int height = mScreenHeight * 3 / 5;
        mMaxPeekHeight = height;
        ViewGroup.LayoutParams params = mBottomSheet.getLayoutParams();
        params.height = height;
    }

    /**
     * ??????????????????
     */
    private void setLocationStyle() {
        Log.d("zh","???????????????");
        // ???????????????????????????
        if (null == mLocationStyle) {
            mLocationStyle = new MyLocationStyle();
            mLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            mLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//???????????????,????????????
        }
        //???????????????????????????????????????????????????????????????????????????????????? ????????????????????????????????
        aMap.setMyLocationStyle(mLocationStyle.myLocationType(mMapType));
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        Log.i("amap","onTouch ?????????????????????????????????????????????");
        useMoveToLocationWithMapMode = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
        aMap = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        useMoveToLocationWithMapMode = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        useMoveToLocationWithMapMode = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onGPSClick() {

        Log.d("zh","??????GPs");
        CameraUpdate cameraUpdate = null;
        mMoveToCenter = true;
        isPoiClick = false;
        //????????????????????????
        switch (mCurrentGpsState){
            case STATE_LOCKED:
                mZoomLevel = 18;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_ROTATE;
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng,mZoomLevel,30,0));
                break;
            case STATE_UNLOCKED:
            case STATE_ROTATE:
                mZoomLevel = 16;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_LOCKED;
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLatLng,mZoomLevel,30,0));
                break;
        }
        //????????????POI??????
        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showPoiDetail("????????????", String.format("???%s??????", mPoiName));
            moveGspButtonAbove();
        }else{
            mTvLocTitle.setText("????????????");
            mTvLocation.setText(String.format("???%s??????", mPoiName));
        }

        aMap.setMyLocationEnabled(true);
        //????????????????????????
        gpsView.setGpsState(mCurrentGpsState);
        //??????????????????
        aMap.animateCamera(cameraUpdate,mAnimDuartion, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });

        setLocationStyle();
        resetLocationMarker2();
        mgo.setVisibility(View.GONE);
    }

    /**
     * ??????????????????????????????????????????
     */
    public void resetLocationMarker(){
//        aMap.clear();
        mLocMarker = null;
        if(gpsView.getGpsState() == GPSView.STATE_ROTATE){
            Log.d("gps","?????????1");
            //ROTATE??????????????????????????????
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(mLatLng);
        }else{
//            Log.d("gps","?????????2");
//            addMarker(mLatLng);
            if(null != mLocMarker){
                mSensorHelper.setCurrentMarker(mLocMarker);
            }
        }
//        addCircle(mLatLng,mAccuracy);
    }
    public void resetLocationMarker2(){
        aMap.clear();
        mLocMarker = null;
        if(gpsView.getGpsState() == GPSView.STATE_ROTATE){
            //ROTATE??????????????????????????????
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(mLatLng);
        }else{
            addMarker(mLatLng);
            if(null != mLocMarker){
                mSensorHelper.setCurrentMarker(mLocMarker);
            }
        }
        addCircle(mLatLng,mAccuracy);
    }
    public void resetLocationMarker3(){
        aMap.clear();
        mLocMarker = null;
        if(gpsView.getGpsState() == GPSView.STATE_ROTATE){
            //ROTATE??????????????????????????????
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(mLatLng);
        }else{
            addMarker(mLatLng);
            if(null != mLocMarker){
                mSensorHelper.setCurrentMarker(mLocMarker);
            }
        }
        addCircle(mLatLng,mAccuracy);
        if(mEndPoi != null){
            addPOIMarderAndShowDetail(mEndPoi.getCoordinate(),mEndPoi.getName());
        }

    }

    /**
     * ??????????????????
     */
    private void hideMapView(){
//        mapView.setVisibility(View.GONE);
        mMapHeaderView.setVisibility(View.GONE);
        mTrafficView.setVisibility(View.GONE);
        gpsView.setVisibility(View.GONE);
        mgo.setVisibility(View.GONE);
        mBehavior.setHideable(true);
        resetGpsButtonPosition();
        hidePoiDetail();
        uiSettings.setAllGesturesEnabled(false);
    }
    private void hideMapView2(){
//        mapView.setVisibility(View.GONE);
        mMapHeaderView.setVisibility(View.GONE);
        mTrafficView.setVisibility(View.GONE);
        gpsView.setVisibility(View.GONE);
        mBehavior.setHideable(true);
        resetGpsButtonPosition();
        hidePoiDetail();
        mgo.setVisibility(View.GONE);
//        uiSettings.setAllGesturesEnabled(false);
    }

    /**
     * ??????????????????
     */
    private void showMapView(){
        mapView.setVisibility(View.VISIBLE);
        mMapHeaderView.setVisibility(View.VISIBLE);
        mTrafficView.setVisibility(View.VISIBLE);
        gpsView.setVisibility(View.VISIBLE);
//        mgo.setVisibility(View.VISIBLE);
        mLLSearchContainer.setVisibility(View.GONE);
        uiSettings.setAllGesturesEnabled(true);
    }

    /**
     * ??????????????????poi?????????
     */
    private void showPoiNameText(String locInfo) {
        mTvLocation.setText(locInfo);
    }


    @Override
    public void onClick(View v) {
        if (v == null){
            return;
        }
        // ????????????POI detail
        if(v == mPoiColseView){
            mBehavior.setHideable(true);
            resetGpsButtonPosition();
            hidePoiDetail();
            mgo.setVisibility(View.VISIBLE);
            return;
        }

        if (v == mgo){
            isTvGo = true;
            hideMapView2();
            mTopLayout.setVisibility(View.VISIBLE);
        }

//        ??????????????????
        if(v == mTvRoute){
            Log.d("mTvRoute","??????????????????");
            if(mLatLng == null){
                Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();
                return;
            }
            if(mClickPointLatLng == null){
                Toast.makeText(getActivity(),"??????????????????",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(),WalkRouteNaviAcitivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("startLatLng", mLatLng);
            bundle.putParcelable("stopLatLng", mClickPointLatLng);

            intent.putExtra("params", bundle);
            startActivity(intent);
            return;
        }

        //??????????????????
        if(v == mTvCall){
            if(checkAppInstalled(getActivity(),"com.sdu.didi.psnger")){
                Intent intent = requireActivity().getPackageManager().getLaunchIntentForPackage("com.sdu.didi.psnger");
                startActivity(intent);
            }else{
                Uri uri = Uri.parse("https://a.app.qq.com/o/simple.jsp?pkgname=com.sdu.didi.psnger&fromcase=10000&g_f=1113784&bd_vid=11288422589755026385");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
                Toast.makeText(getActivity(),"????????????????????????????????????????????????",Toast.LENGTH_SHORT).show();
            }
        }
        //?????????????????????
        if(v == mtvGo){
            if(mEndPoi == null){
                return;
            }
            isTvGo = true;
            routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
//            hideAll();

        }


        //??????????????????
        if(v == mIvLeftSearch){
            hideSearchTipView();
            showMapView();
            return;
        }
        if(v == back){
            hideRouted();
            showMapView();
            isTvGo = false;
            resetLocationMarker3();
            mBusResultRview.setVisibility(View.GONE);
            aMap.getUiSettings().setRotateGesturesEnabled(false);
        }

        if(v == mFromText){
            Location location1=aMap.getMyLocation();
            PoiSearchActivity2.start(mContext,mLocation.getExtras(),mLocation.getLatitude(),mLocation.getLongitude(),PoiSearchActivity2.FROM_START);
            routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
        }
        if(v == mTargetText){

            Location location2=aMap.getMyLocation();
            PoiSearchActivity2.start(mContext,mLocation.getExtras(),mLocation.getLatitude(),mLocation.getLongitude(),PoiSearchActivity2.FROM_TARGET);
            routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
        }
        if(v == change){
            Poi temp=mStartPoi;
            mStartPoi=mEndPoi;
            mEndPoi=temp;
            updateEditUI();
            routeSearch(mStartPoi,mEndPoi, mSelectedType);
        }
        if(v == startbtn || v == startbtn1){
            Log.d("mTvRoute","??????????????????");
            if(mLatLng == null){
                Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();
                return;
            }
            if(mClickPointLatLng == null){
                Toast.makeText(getActivity(),"??????????????????",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(),WalkRouteNaviAcitivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("startLatLng", mLatLng);
            bundle.putParcelable("stopLatLng", mClickPointLatLng);

            intent.putExtra("params", bundle);
            startActivity(intent);
        }
        if(v == pathlayout ){
            onPathClick(0);
        }
        if(v == pathlayout1 ){
            onPathClick(1);
        }
        if(v == pathlayout2 ){
            onPathClick(2);
        }
//????????????
        if(v==mFrequentView){
            Log.d("mFrequentView","mFrequentView?????????");
            Intent intent = new Intent(getActivity(),SearchActivity.class);
            Bundle bundle=new Bundle();
            startActivity(intent);
            return;
        }

    }
    private int getSheetHeadHeight(){
        mSheetHeadLayout.measure(0,0);
        Log.d("czh",mSheetHeadLayout.getMeasuredHeight()+"height");
        return mSheetHeadLayout.getMeasuredHeight();
    }

    private int getTopLayoutHeight(){
//        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
        Log.d("czh",mTopLayoutHeight+"top height");
        return mTopLayout.getHeight();
    }

    //????????????
    private void routeSearch(Poi startPoi,Poi targetPoi, int type){
        if(startPoi == null || targetPoi == null){
            return;
        }
        LatLng start = startPoi.getCoordinate();
        LatLng target = targetPoi.getCoordinate();

        RouteSearch routeSearch = new RouteSearch(getActivity());
        routeSearch.setRouteSearchListener(this);
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(AMapServicesUtil.convertToLatLonPoint(start),AMapServicesUtil.convertToLatLonPoint(target));
        switch (type){
            case TYPE_DRIVE:
                RouteSearch.DriveRouteQuery dquery=new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST,null,null,"");
                routeSearch.calculateDriveRouteAsyn(dquery);
                break;
            case TYPE_BUS:
                RouteSearch.BusRouteQuery bquery=new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT,
                        mCity,0);
                routeSearch.calculateBusRouteAsyn(bquery);
                break;
            case TYPE_WALK:
                RouteSearch.WalkRouteQuery wquery=new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT );
                routeSearch.calculateWalkRouteAsyn(wquery);
                break;
            case TYPE_RIDE:
                RouteSearch.RideRouteQuery rquery=new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RIDING_DEFAULT );
                routeSearch.calculateRideRouteAsyn(rquery);
                break;
            default:
                break;
        }
    }

    //????????????
    @Override
    public void onUserClick(View v) {
//        aMap = null;
//        NavController controller = Navigation.findNavController(v);
//        controller.navigate(R.id.action_homeFragment_to_userFragment);
        startActivity(new Intent(getActivity(), UserActivity.class));
    }

    @Override
    public void onSearchClick() {
// ????????????layout,??????????????????,?????????????????????????????????
        showSearchTipView();
        hideMapView();
//        mMapMode = MapMode.SEARCH;
    }
    /**
     * ????????????layout
     */
    private void showSearchTipView(){
        mgo.setVisibility(View.GONE);
        mLLSearchContainer.setVisibility(View.VISIBLE);
        InputMethodUtils.showInput(getActivity(),mEtSearchTip);

    }

    @Override
    public void onTrafficChanged(boolean selected) {
        aMap.setTrafficEnabled(selected);
    }

    private void addPOIMarker(LatLng latLng) {
        aMap.clear();
        addMarker(mLatLng);
        mSensorHelper.setCurrentMarker(mLocMarker);
        MarkerOptions markOptiopns = new MarkerOptions();
        markOptiopns.position(latLng);
        markOptiopns.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_mark));
        aMap.addMarker(markOptiopns);
    }

    /**
     * ????????????????????????
     */
    private void hideSearchTipView(){
        InputMethodUtils.hideInput(getActivity());
        mgo.setVisibility(View.VISIBLE);
        mLLSearchContainer.setVisibility(View.GONE);
        mEtSearchTip.setVisibility(View.VISIBLE);
        mEtSearchTip.setFocusable(true);
        mEtSearchTip.setFocusableInTouchMode(true);
        mSearchData.clear();
        mSearchAdapter.notifyDataSetChanged();
        mEtSearchTip.setText("");
    }


    /**
     * ??????poi????????????BottomSheet
     */
    private void showClickPoiDetail(LatLng latLng, String poiName) {
        mPoiName = poiName;
        mTvLocTitle.setText(poiName);
        String distanceStr = MyAMapUtils.calculateDistanceStr(mLatLng, latLng);
        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showPoiDetail(poiName, String.format("?????????%s", distanceStr));
            moveGspButtonAbove();
        }else{
            mTvLocTitle.setText(poiName);
            mTvLocation.setText(String.format("?????????%s", distanceStr));
        }
    }


    /**
     * ????????????????????????????????????
     * @param latLng
     */
    private void animMap(LatLng latLng){
        if(latLng != null){
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel));
        }
    }

    /**
     * ??????POImarker
     */
    private void addPOIMarderAndShowDetail(LatLng latLng,String poiName){
        animMap(latLng);
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        //??????????????????????????????????????????
        if (!mFirstLocation) {
            gpsView.setGpsState(mCurrentGpsState);
        }
        mMoveToCenter = false;
        // ??????marker??????
        addPOIMarker(latLng);
        showClickPoiDetail(latLng, poiName);
    }

    @Override
    public void onPOIClick(Poi poi) {
        Log.d("poi","poi??????");
        if(poi == null || poi.getCoordinate() == null || TextUtils.isEmpty(poi.getName())){
            return;
        }

        mEndPoi = poi;
        mClickPointLatLng = poi.getCoordinate();// ??????????????????
        isPoiClick = true;// ??????????????????poi??????
        mTargetText.setText(poi.getName());
        if(isTvGo == true){
            routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
        }else{
            addPOIMarderAndShowDetail(poi.getCoordinate(), poi.getName());
        }


        Log.d("position3", String.valueOf(poi.getName()));
        Log.d("position3", String.valueOf(poi.getCoordinate()));
        mgo.setVisibility(View.GONE);

    }


    @Override
    public void showPoiDetail(String locTitle, String locInfo) {
        gpsView.setVisibility(View.VISIBLE);

        mBottomSheet.setVisibility(View.VISIBLE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mPoiDetailTaxi.setVisibility(View.VISIBLE);
        //????????????
        mTvLocTitle.setText(locTitle);
        mTvLocation.setText(locInfo);
        int poiTaxiHeight = getResources().getDimensionPixelSize(R.dimen.setting_item_large_height);

        mBehavior.setHideable(true);
        mBehavior.setPeekHeight(mMinPeekHeight + poiTaxiHeight);
        mgo.setVisibility(View.GONE);
    }

    /**
     * ????????????POI??????
     */
    @Override
    public void hidePoiDetail() {
        mBottomSheet.setVisibility(View.GONE);
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mPoiDetailTaxi.setVisibility(View.GONE);
    }

    @Override
    public void showBackToMapState() {
        mPoiDetailTaxi.setPoiDetailState(PoiDetailBottom.STATE_MAP);
    }

    @Override
    public void showPoiDetailState() {
        mPoiDetailTaxi.setPoiDetailState(PoiDetailBottom.STATE_DETAIL);
    }

    @Override
    public void minMapView() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
        //??????????????????LayoutParams
        if (lp.bottomMargin == mMaxPeekHeight) {
            return;
        }
        lp.bottomMargin = mMaxPeekHeight;
        mapView.setLayoutParams(lp);
    }

    @Override
    public void maxMapView() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();
        //??????????????????LayoutParams
        if (lp.bottomMargin == 0) {
            return;
        }
        lp.bottomMargin = 0;
       mapView.setLayoutParams(lp);
    }

    @Override
    public void onPoiDetailCollapsed() {
//BottomSheet??????????????????????????????????????????????????????????????????
//        mgo.setVisibility(View.VISIBLE);
        mPoiColseView.setVisibility(View.VISIBLE);
        mMapHeaderView.setVisibility(View.VISIBLE);
        gpsView.setVisibility(View.VISIBLE);
        mTrafficView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPoiDetailExpanded() {
//BottomSheet??????????????????????????????????????????????????????????????????
        mgo.setVisibility(View.GONE);
        mMapHeaderView.setVisibility(View.GONE);
        gpsView.setVisibility(View.GONE);
        mTrafficView.setVisibility(View.GONE);
    }
    /**
     * ?????????????????????????????????marker
     */

    @Override
    public void smoothSlideUpMap() {
        Log.d("upBottom","??????????????????");
        switch (gpsView.getGpsState()){
            case GPSView.STATE_ROTATE:
                if(!isPoiClick){
                    mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                }
                break;
                case GPSView.STATE_UNLOCKED:
                case GPSView.STATE_LOCKED:
                    if(!isPoiClick){
                        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                    }
                    break;
        }
        setLocationStyle();
        aMap.getUiSettings().setAllGesturesEnabled(false);
        if(!isPoiClick){
            mMoveToCenter = true;
        }else{
            mMoveToCenter = false;
        }
        ViewGroup.LayoutParams lp = mapView.getLayoutParams();
        lp.height = mScreenHeight * 2 / 5;
        mapView.setLayoutParams(lp);
        Log.d("upBottom","????????????2/5");
    }

    @Override
    public void smoothSlideDownMap() {
        Log.d("upBottom","??????????????????");
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mMoveToCenter = false;
        slideDown = true;
        ViewGroup.LayoutParams lp = mapView.getLayoutParams();
        lp.height = mScreenHeight;
        mapView.setLayoutParams(lp);
        //??????????????????
        aMap.getUiSettings().setAllGesturesEnabled(true);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        switch (gpsView.getGpsState()) {
            case GPSView.STATE_ROTATE:
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                break;
            case GPSView.STATE_UNLOCKED:
            case GPSView.STATE_LOCKED:
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                break;
        }
        setLocationStyle();
//        resetLocationMarker();
        mMoveToCenter = false;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param v
     * @param v1
     */
    @Override
    public void onDoubleTap(float v, float v1) {
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        gpsView.setGpsState(mCurrentGpsState);
        setLocationStyle();
        resetLocationMarker();
        mMoveToCenter = false;
    }

    @Override
    public void onSingleTap(float v, float v1) {

    }

    @Override
    public void onFling(float v, float v1) {

    }

    /**
     * ???????????????????????????????????????
     *
     * @param v
     * @param v1
     */
    @Override
    public void onScroll(float v, float v1) {
        //????????????????????????????????????up????????????false
        if (!onScrolling) {
            onScrolling = true;
            //???????????????????????????
            mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
            mCurrentGpsState = STATE_UNLOCKED;
            //??????????????????????????????????????????
            if (!mFirstLocation) {
                gpsView.setGpsState(mCurrentGpsState);
            }
            mMoveToCenter = false;
            setLocationStyle();
            resetLocationMarker();
        }
    }

    @Override
    public void onLongPress(float v, float v1) {

    }

    @Override
    public void onDown(float v, float v1) {

    }

    @Override
    public void onUp(float v, float v1) {
        onScrolling = false;
    }

    @Override
    public void onMapStable() {

    }

    @Override
    public void onDetailClick() {
        int state = mPoiDetailTaxi.getPoiDetailState();
        switch (state) {
            case PoiDetailBottom.STATE_DETAIL:
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                //minMapView();
                break;
            case PoiDetailBottom.STATE_MAP:
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


                break;
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        Log.d("position2",mCity);
        Log.d("position2","????????????");

        if(mSearchData != null && mSearchData.size() > 0){
            Tip tip = mSearchData.get(position);
            if(tip == null){
                return;
            }
            Log.d("position2", String.valueOf(tip));
            Log.d("position2", String.valueOf(tip.getPoint()));

            hideSearchTipView();
            showMapView();
            mMoveToCenter = false;
            isPoiClick = true;
            LatLonPoint point = tip.getPoint();
            if( point!= null){
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                addPOIMarderAndShowDetail(latLng, tip.getName());
                showClickPoiDetail(latLng, tip.getName());
                mClickPointLatLng = latLng;
                mEndPoi = new Poi(tip.getName(),latLng,"");
                mTargetText.setText(mEndPoi.getName());
                Log.d("poi", mEndPoi.getName());
                Log.d("position2", "????????????");
            }

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s == null || TextUtils.isEmpty(s.toString())){
            mSearchProgressBar.setVisibility(View.GONE);
            return;
        }
        String content = s.toString();
        if(!TextUtils.isEmpty(content) && ! TextUtils.isEmpty(mCity)){
            // ??????????????????????????????api
            InputtipsQuery inputquery = new InputtipsQuery(content, mCity);
            inputquery.setCityLimit(false);
            Inputtips inputTips = new Inputtips(getActivity(), inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
            mSearchProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        mSearchProgressBar.setVisibility(View.GONE);
        if(list == null || list.size() == 0){
            return;
        }
        mSearchData.clear();
        mSearchData.addAll(list);
        // ??????RecycleView
        mSearchAdapter.notifyDataSetChanged();

    }

    private void initTabLayout(){
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_drive));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_ride));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_walk));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_bus));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                mTabLayout.selectTab(mTabLayout.getTabAt(0));
                if (getString(R.string.route_plan_walk).equals(tab.getText())){
                    Log.d("tab","????????????");
                    mSelectedType =TYPE_WALK;
                    if (mEndPoi==null){
                        return;
                    }
                    Location location=aMap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_WALK);
                }
                else if (getString(R.string.route_plan_drive).equals(tab.getText())){
                    Log.d("tab","????????????");
                    mSelectedType =TYPE_DRIVE;
                    if (mEndPoi==null){
                        return;
                    }
                    Location myLocation=aMap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
                }
                else if (getString(R.string.route_plan_ride).equals(tab.getText())) {
                    Log.d("tab","????????????");
                    mSelectedType = TYPE_RIDE;
                    if (mEndPoi == null) {
                        return;
                    }
                    Location location=aMap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_RIDE);
                }else if (getString(R.string.route_plan_bus).equals(tab.getText())){
                    Log.d("tab","????????????");
                        mSelectedType =TYPE_BUS;
                        if (mEndPoi==null){
                            return;
                        }
                        routeSearch(mStartPoi,mEndPoi,TYPE_BUS);
                    }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        if (i==1000){
            Log.d("select","??????bus??????");
            if (busRouteResult != null && busRouteResult.getPaths() != null){
                if (busRouteResult.getPaths().size() > 0){
                    if (mTopLayout.getVisibility()!=View.VISIBLE){
                        ViewAnimUtils.dropDownWithInterpolator(mTopLayout, new ViewAnimUtils.AnimEndListener() {
                            @Override
                            public void onAnimEnd() {
                                mTopLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    mNesteScrollView.setVisibility(View.GONE);

                    mBusRouteResult = busRouteResult;
                    mBusResultAdapter=new BusResultListAdapter(mContext,busRouteResult);
                    mBusResultRview.setAdapter(mBusResultAdapter);
                    ViewAnimUtils.popupinWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                        @Override
                        public void onAnimEnd() {
                            mBusResultRview.setVisibility(View.VISIBLE);
                        }
                    });
//                    drawBusRoutes(mBusRouteResult,mBusRouteResult.getPaths().get(0));
                }else if (busRouteResult != null && busRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i==1000){
            Log.d("select","??????drive??????");
            if (driveRouteResult != null && driveRouteResult.getPaths() != null){
                if (driveRouteResult.getPaths().size() > 0){
                    updateUiAfterRouted();
                    mDriveRouteResult=driveRouteResult;

                    DrivePath path=mDriveRouteResult.getPaths().get(0);
                    mDrivePathAdapter=new DrivePathAdapter(getActivity(),path.getSteps());
                    Log.d("step", String.valueOf(mDrivePathAdapter));
                    mPathDetailRecView.setAdapter(mDrivePathAdapter);
                    Log.d("step", String.valueOf(mPathDetailRecView));
                    mPathDetailRecView.setVisibility(View.VISIBLE);

                    mPathTipsText.setText(getString(R.string.route_plan_path_traffic_lights,path.getTotalTrafficlights()+""));
                    mPathTipsText.setVisibility(View.VISIBLE);
                    drawDriveRoutes(mDriveRouteResult,path);

                    for (int j=0;j<mDriveRouteResult.getPaths().size();j++){
                        updatePathGeneral(mDriveRouteResult.getPaths().get(j),j);
                    }

                    mBehavior2.setPeekHeight(getSheetHeadHeight());
                }else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        Log.d("Search", String.valueOf(i));
        if (i==1000){
            if (walkRouteResult != null && walkRouteResult.getPaths() != null){
                if (walkRouteResult.getPaths().size() > 0){
                    updateUiAfterRouted();

                    mWalkRouteResult = walkRouteResult;
                    WalkPath path=mWalkRouteResult.getPaths().get(0);
                    mWalkStepAdapter=new WalkStepAdapter(mContext,path.getSteps());
                    mPathDetailRecView.setAdapter(mWalkStepAdapter);
//                    mPathDetailRecView.setVisibility(View.VISIBLE);
                    drawWalkRoutes(mWalkRouteResult,mWalkRouteResult.getPaths().get(0));

                    for (int j=0;j<mWalkRouteResult.getPaths().size();j++){
                        updatePathGeneral(mWalkRouteResult.getPaths().get(j),j);
                    }

                    mBehavior2.setPeekHeight(getSheetHeadHeight());
                }else if (walkRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,"???????????????????????????????????????????????????????????????????????????",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        if (i==1000){
            if (rideRouteResult != null && rideRouteResult.getPaths() != null){
                if (rideRouteResult.getPaths().size() > 0){
                    updateUiAfterRouted();
                    mRideRouteResult = rideRouteResult;

                    RidePath path=mRideRouteResult.getPaths().get(0);
                    mRideStepAdapter=new RideStepAdapter(mContext,path.getSteps());
                    mPathDetailRecView.setVisibility(View.VISIBLE);
                    drawRideRoutes(mRideRouteResult,mRideRouteResult.getPaths().get(0));

                    for (int j=0;j<mRideRouteResult.getPaths().size();j++){
                        updatePathGeneral(mRideRouteResult.getPaths().get(j),j);
                    }

                    mBehavior2.setPeekHeight(getSheetHeadHeight());
                }else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    private void hideRouted(){
        mTopLayout.setVisibility(View.GONE);
        mNesteScrollView.setVisibility(View.GONE);
    }

    private void onPathClick(int i){
        switch (mSelectedType){
            case TYPE_DRIVE:
                mPathTipsText.setText(getString(R.string.route_plan_path_traffic_lights,mDriveRouteResult.getPaths().get(i).getTotalTrafficlights()+""));
                mPathDetailRecView.setAdapter(new DrivePathAdapter(mContext,mDriveRouteResult.getPaths().get(i).getSteps()));
                drawDriveRoutes(mDriveRouteResult,mDriveRouteResult.getPaths().get(i));
                break;
            case TYPE_WALK:
                mPathDetailRecView.setAdapter(new WalkStepAdapter(mContext,mWalkRouteResult.getPaths().get(i).getSteps()));
                drawWalkRoutes(mWalkRouteResult,mWalkRouteResult.getPaths().get(i));
                break;
            case TYPE_RIDE:
                mPathDetailRecView.setAdapter(new RideStepAdapter(mContext,mRideRouteResult.getPaths().get(i).getSteps()));
                drawRideRoutes(mRideRouteResult,mRideRouteResult.getPaths().get(i));
                break;
            default:
                break;
        }
    }

    private void updateEditUI(){
        if (mStartPoi==null){
            mFromText.setText("");
//            mFromText.setText(getString(R.string.poi_search_my_location));
        }else {
            mFromText.setText(mStartPoi.getName());
        }
        if (mEndPoi==null){
            mTargetText.setText("");
        }else {
            mTargetText.setText(mEndPoi.getName());
        }
    }

    private void updateUiAfterRouted(){
        if (mTopLayout.getVisibility()!=View.VISIBLE){
            ViewAnimUtils.dropDownWithInterpolator(mTopLayout, new ViewAnimUtils.AnimEndListener() {
                @Override
                public void onAnimEnd() {
                    mTopLayout.setVisibility(View.VISIBLE);
                }
            });
        }
        if (mBusResultRview.getVisibility()==View.VISIBLE){
            ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                @Override
                public void onAnimEnd() {
                    mBusResultRview.setVisibility(View.GONE);

                }
            });
        }
        hideMapView2();
        mNaviText.setVisibility(View.VISIBLE);
        mNesteScrollView.setVisibility(View.VISIBLE);
    }
    //????????????
    private void drawDriveRoutes(DriveRouteResult driveRouteResult, DrivePath path){
        aMap.clear();
        final DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                mContext, aMap, path,
                driveRouteResult.getStartPos(),driveRouteResult.getTargetPos(),
                null);
        drivingRouteOverlay.setNodeIconVisibility(false);//????????????marker????????????
        drivingRouteOverlay.setIsColorfulline(true);//????????????????????????????????????????????????true
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void drawBusRoutes(BusRouteResult busRouteResult, BusPath path){
        aMap.clear();
        BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                mContext, aMap,path,busRouteResult.getStartPos(),
                busRouteResult.getTargetPos());
        busRouteOverlay.setNodeIconVisibility(true);//????????????marker????????????
        busRouteOverlay.removeFromMap();
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void drawWalkRoutes(WalkRouteResult walkRouteResult, WalkPath path){
        aMap.clear();
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                mContext, aMap,path,walkRouteResult.getStartPos(),
                walkRouteResult.getTargetPos());
        walkRouteOverlay.setNodeIconVisibility(true);//????????????marker????????????
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void drawRideRoutes(RideRouteResult rideRouteResult, RidePath path){
        aMap.clear();
        RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                mContext, aMap,path,rideRouteResult.getStartPos(),
                rideRouteResult.getTargetPos());
        rideRouteOverlay.setNodeIconVisibility(true);//????????????marker????????????
        rideRouteOverlay.removeFromMap();
        rideRouteOverlay.addToMap();
        rideRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void updatePathGeneral(Path path, int i){
        String dur = AMapUtil.getFriendlyTime((int) path.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) path.getDistance());
        if (i==0){
            mPathDurText.setText(dur);
            mPathDisText.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.GONE);
            mPathLayout2.setVisibility(View.GONE);
        }else if (i==1){
            mPathDurText1.setText(dur);
            mPathDisText1.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.VISIBLE);
            mPathLayout2.setVisibility(View.GONE);

        }else if (i==2){
            mPathDurText2.setText(dur);
            mPathDisText2.setText(dis);
            mPathLayout.setVisibility(View.VISIBLE);
            mPathLayout1.setVisibility(View.VISIBLE);
            mPathLayout2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(BusPath busPath) {
        drawBusRoutes(mBusRouteResult,busPath);
    }


    /**
     * ????????????
     */
    private enum MapMode{
        /**
         * ????????????:??????????????????
         */
        NORMAL,

        /**
         * ????????????:?????????????????????????????????
         */
        SEARCH
    }

    /**
     * ??????ViewHolder
     */
    private static class SearchViewHolder extends RecyclerView.ViewHolder{
        TextView tvSearchTitle;
        TextView tvSearchLoc;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchTitle = itemView.findViewById(R.id.tv_search_title);
            tvSearchLoc = itemView.findViewById(R.id.tv_search_loc);
        }
    }


    /**
     * ??????Adapter
     */
    private  class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> implements View.OnClickListener{

        private List<Tip> mData;
        private OnItemClickListener mListener;


        public SearchAdapter(List<Tip> data){
            this.mData = data;
        }


        /**
         * ??????RecycleView????????????
         * @param listener
         */
        public void setOnItemClickListener(OnItemClickListener listener){
            this.mListener = listener;
        }


        @Override
        public void onClick(View v) {

        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = ((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.search_tip_recycle, parent, false);
            itemView.setTag(viewType);
            itemView.setOnClickListener(this);
            return new SearchViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, final int position) {
//            Log.d("position", String.valueOf(position));
            Tip tip = mData.get(position);
            holder.tvSearchTitle.setText(tip.getName());
            holder.tvSearchLoc.setText(tip.getAddress());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null){
                        mListener.onItemClick(view,position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            if(mData != null && mData.size() > 0){
                return mData.size();
            }
            return 0;
        }
    }
    //???????????????????????????
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
    /**
     * ??????????????????
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectPoiEvent(PoiItemEvent event){
        PoiItem item=event.getItem();
        LatLonPoint point=item.getLatLonPoint();
        LatLng latLng=new LatLng(point.getLatitude(),point.getLongitude());
        if (event.getFrom()== PoiSearchActivity.FROM_START){
            mStartPoi=new Poi(item.getTitle(),latLng,item.getAdName());
        }else if (event.getFrom()==PoiSearchActivity.FROM_TARGET){
            mEndPoi=new Poi(item.getTitle(),latLng,item.getAdName());
        }
        updateEditUI();
//        goToPlaceAndMark(item);

        mPoiTitleText.setText(item.getTitle());
        mPoiDescText.setText(item.getAdName()+"    "+item.getSnippet());

        if (mStartPoi==null || mEndPoi==null){
            return;
        }
        routeSearch(mStartPoi,mEndPoi,mSelectedType);
    }



    /**
     * ????????????????????????
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectedMyPoiEvent(SelectedMyPoiEvent event){
        Location location=aMap.getMyLocation();
        if (event.getFrom()==PoiSearchActivity.FROM_START){
            mStartPoi=new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(),location.getLongitude()),"");
        }else if (event.getFrom()==PoiSearchActivity.FROM_TARGET){
            mEndPoi=new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(),location.getLongitude()),"");
        }
        updateEditUI();
    }

    //????????????????????????
    private boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName== null || pkgName.isEmpty()) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if(info == null || info.isEmpty())
            return false;
        for ( int i = 0; i < info.size(); i++ ) {
            if(pkgName.equals(info.get(i).packageName)) {
                return true;
            }
        }
        return false;
    }
}