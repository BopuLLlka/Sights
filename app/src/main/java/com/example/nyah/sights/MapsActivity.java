package com.example.nyah.sights;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import ru.yandex.speechkit.SpeechKit;



import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int PERMISSION_REQUEST = 1;

    private double lat;
    private double lon;

    private static final double SIGHT_AREA = 0.001;

    private ArrayList<Sight> sights = new ArrayList<Sight>();

    private RelativeLayout relativeLayout;
    private LinearLayout currentSightMenu;

    private ImageView imageView;
    private TextView nameText;
    private TextView descriptionText;
    private Button closeButton;
    private TextView currentSight;

    //Id достопримечательности, которая находится рядом
    private int currentSightId;

    private Button openButton;

    private SpeechKit speechKit;

    //Контейнер для отладки
    private TextView logText;

    //Спикер, который будет зачитывать текст
    private TextToSpeech speaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //speechKit.getInstance().configure(getApplicationContext(),"8bad8bcc-a19d-46e8-a6bb-2b0eeb70f841");

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Locale locale = new Locale("ru");
                    speaker.setLanguage(locale);
                }
            }
        });

        //Текстовое поле для вывода сообщений при отладке
        logText = (TextView)findViewById(R.id.logText);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        currentSightMenu = (LinearLayout)findViewById(R.id.currentSightMenu);

        relativeLayout = (RelativeLayout)findViewById(R.id.relative_layout);
        //Фото достопримеательности
        imageView = (ImageView)findViewById(R.id.imageView);
        //Название текущей достопримечательности
        currentSight = (TextView)findViewById(R.id.currentSight);
        //Название достопримечательности в окне
        nameText = (TextView)findViewById(R.id.nameText);
        //Описание достопримечательности в окне
        descriptionText = (TextView)findViewById(R.id.descriptionText);

        currentSightMenu.setVisibility(View.INVISIBLE);

        //Кнопка, которая скрывает экран с описанием
        closeButton = (Button)findViewById(R.id.closeButton);
        //Событие нажатия на кнопку для скрытия информации о достопримечательности
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentSightMenu.setVisibility(View.VISIBLE);
                relativeLayout.setVisibility(View.INVISIBLE);

            }
        });
        //Кнопка, которая позволяет открыть достопримечательность
        openButton = (Button)findViewById(R.id.openButton);
        //Открыть текущую достопримечательность
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSight(sights.get(currentSightId));
            }
        });


        descriptionText.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private LocationListener locationListener = new LocationListener() {

        CameraUpdate cameraUpdate;

        //Когда позиция изменилась
        @Override
        public void onLocationChanged(Location location) {

            for(int i = 0; i<sights.size(); i++){
                Sight item = sights.get(i);
                //Если в зоне видимости достопримечательность
                if( (lat >= item.getLat() - SIGHT_AREA && lat <= item.getLat() + SIGHT_AREA) && (lon >= item.getLon() - SIGHT_AREA && lon <= item.getLon() + SIGHT_AREA) ){
                    currentSightMenu.setVisibility(View.VISIBLE);
                    setCurrentSight(item, i);
                    setSight(item);
                }
            }
            showLocation(location);
        }

        private void showLocation(Location location) {
            if (location == null)
                return;
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            } else if (location.getProvider().equals(
                    LocationManager.NETWORK_PROVIDER)) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
            }
        }

        //Когда провайдер выключен
        @Override
        public void onProviderDisabled(String provider) {

        }
        //Когда включился провайдер для нахождения локации
        @Override
        public void onProviderEnabled(String provider) {

        }
        //Когда изменился статус
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {

            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

            }
        }
    };

    public void setCurrentSight(Sight item, int i)
    {
        currentSightId = i;
        currentSight.setText("Рядом: "+item.getName());
    }

    //Вывод окна с  информацией о достопримечательности
    public void setSight(Sight item)
    {
        currentSightMenu.setVisibility(View.INVISIBLE);
        //Открываем окно с выводом информации
        relativeLayout.setVisibility(View.VISIBLE);
        //Устанавливаем имя достопримечательности
        nameText.setText(item.getName());
        //Устанавливаем описание достопримечательности
        descriptionText.setText(item.getDescription());
        //Устанавливаем картинку достопримечательности
        new DownLoadImageTask(imageView).execute(item.getImagePath());
        //Говорим голосом когда открывается информация о достопримечательности
        speaker.speak(item.getDescription(), TextToSpeech.QUEUE_FLUSH, null);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
            }
        else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                mMap.setMyLocationEnabled(true);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location!=null)
                {
                    lat=location.getLatitude();
                    lon=location.getLongitude();
                }
                else
                {
                    nameText.setText("else!");
                }

                createSightMap();
                drawMarkers();
            }

        //Устанавливаем слушателя на клик по маркеру
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String tempString="";
                //Ищем достопримечательность от этого маркера
                for (int i=0; i<sights.size();i++)
                {
                    Sight item = sights.get(i);
                    tempString+="1:"+marker.getId()+" 2:"+item.Marker.getId()+"";

                    if(marker.getId().equals(item.Marker.getId()))
                    {
                        tempString+="!!!!!!!";
                        setSight(item);
                    }
                    tempString+="\n";
                }
                //logText.setText(tempString);
                return false;
            }
        });
    }

    public void drawMarkers()
    {
       for(int i = 0; i<sights.size(); i++)
       {
           Sight item = sights.get(i);

           LatLng markerLocation = new LatLng(item.getLat(), item.getLon());

           if(item.getType()==1)
           {
               item.Marker = mMap.addMarker(new MarkerOptions()
                       .position(markerLocation)
                       .title(item.getName())
                       .snippet(item.getDescription())
                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
           }
           if(item.getType()==2)
           {
               item.Marker = mMap.addMarker(new MarkerOptions()
                       .position(markerLocation)
                       .title(item.getName())
                       .snippet(item.getDescription())
                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
           }
           if(item.getType()==3)
           {
               item.Marker = mMap.addMarker(new MarkerOptions()
                       .position(markerLocation)
                       .title(item.getName())
                       .snippet(item.getDescription())
                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
           }
       }
    }

    private class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();

                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }

    private void createSightMap(){
        Sight tempSight = new Sight();
        tempSight.setName("Чернавский мост");
        tempSight.setDescription("Чернавский мост");
        tempSight.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight.setLat(51.667077);
        tempSight.setLon(39.230549);
        tempSight.setSity(1);
        tempSight.setType(1);
        sights.add(tempSight);

        Sight tempSight1 = new Sight();
        tempSight1.setName("Памятник Кольцову");
        tempSight1.setLat(51.661493);
        tempSight1.setLon(39.201169);
        tempSight1.setDescription("Памятник Кольцову");
        tempSight1.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight1.setSity(1);
        tempSight1.setType(3);
        sights.add(tempSight1);

        Sight tempSight2 = new Sight();
        tempSight2.setName("Памятник Владимиру Высоцкому");
        tempSight2.setDescription("Памятник Высоцкому");
        tempSight2.setImagePath("https://upload.wikimedia.org/wikipedia/ru/thumb/a/a2/%D0%9F%D0%B0%D0%BC%D1%8F%D1%82%D0%BD%D0%B8%D0%BA_%D0%92%D1%8B%D1%81%D0%BE%D1%86%D0%BA%D0%BE%D0%BC%D1%83_%28%D0%92%D0%BE%D1%80%D0%BE%D0%BD%D0%B5%D0%B6%29.jpg/800px-%D0%9F%D0%B0%D0%BC%D1%8F%D1%82%D0%BD%D0%B8%D0%BA_%D0%92%D1%8B%D1%81%D0%BE%D1%86%D0%BA%D0%BE%D0%BC%D1%83_%28%D0%92%D0%BE%D1%80%D0%BE%D0%BD%D0%B5%D0%B6%29.jpg");
        tempSight2.setLat(51.666757);
        tempSight2.setLon(39.201811);
        tempSight2.setType(3);
        sights.add(tempSight2);

        Sight tempSight3 = new Sight();
        tempSight3.setName("Памятник славы");
        tempSight3.setDescription("Па́мятник Сла́вы — мемориальный комплекс на братской могиле воинов Советской Армии, погибших в боях за Воронеж во время Великой Отечественной войны в 1942—1943 годах. Установлен в северном микрорайоне на пересечении Московского проспекта и улицы Хользунова. В братской могиле воинов схоронено около 10 тыс. человек. Мемориал был открыт 24 января 1967 года. Авторы мемориала — скульптор Ф. К. Сушков и архитектор А. Г. Бузов. 7 мая 2009 года у Памятника Славы были перезахоронены останки танкового генерала А. И. Лизюкова, погибшего на территории Воронежской области в годы Великой Отечественной войны более чем полувека назад. Там же захоронены останки еще семи неизвестных солдат, найденных поблизости от А.И. Лизюкова (их могилы выделены отдельными стелами) .");
        tempSight3.setImagePath("https://upload.wikimedia.org/wikipedia/ru/5/5b/%D0%9F%D0%B0%D0%BC%D1%8F%D1%82%D0%BD%D0%B8%D0%BA_%D0%A1%D0%BB%D0%B0%D0%B2%D1%8B_%D0%92%D0%BE%D1%80%D0%BE%D0%BD%D0%B5%D0%B6.JPG");
        tempSight3.setLat(51.702686);
        tempSight3.setLon(39.181720);
        tempSight3.setType(3);
        sights.add(tempSight3);

        Sight tempSight4 = new Sight();
        tempSight4.setName("Парк орлёнок");
        tempSight4.setDescription("Оарк орлёнок");
        tempSight4.setImagePath("https://yt3.ggpht.com/a-/AJLlDp2T8CyXUJG-6s7cvNQy6_xLa8NvOagpv0BC2w=s900-mo-c-c0xffffffff-rj-k-no");
        tempSight4.setLat(51.674882);
        tempSight4.setLon(39.206391);
        tempSight4.setType(2);
        sights.add(tempSight4);

        Sight tempSight5 = new Sight();
        tempSight5.setName("Петровсикй сквер");
        tempSight5.setDescription("Петровский сквер");
        tempSight5.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight5.setLat(51.673840);
        tempSight5.setLon(39.211637);
        tempSight5.setType(2);
        sights.add(tempSight5);

        Sight tempSight6 = new Sight();
        tempSight6.setName("Приданческая Дамба");
        tempSight6.setDescription("Дамба");
        tempSight6.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight6.setLat(51.672260);
        tempSight6.setLon(39.235388);
        tempSight6.setType(1);
        sights.add(tempSight6);

        Sight tempSight7 = new Sight();
        tempSight7.setName("Меркурий");
        tempSight7.setDescription("Караблик на воде");
        tempSight7.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight7.setLat(51.625531);
        tempSight7.setLon(39.213434);
        tempSight7.setType(3);
        sights.add(tempSight7);

        Sight tempSight8 = new Sight();
        tempSight8.setName("Памятник паровозу");
        tempSight8.setDescription("Памятник паровозу");
        tempSight8.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight8.setLat(51.679967);
        tempSight8.setLon(39.207758);
        tempSight8.setType(3);
        sights.add(tempSight8);

        Sight tempSight9 = new Sight();
        tempSight9.setName("Собор");
        tempSight9.setDescription("Собор");
        tempSight9.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight9.setLat(51.675723);
        tempSight9.setLon(39.211000);
        tempSight9.setType(1);
        sights.add(tempSight9);

        Sight tempSight10 = new Sight();
        tempSight10.setName("Памятник");
        tempSight10.setDescription("Собор");
        tempSight10.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight10.setLat(51.665899);
        tempSight10.setLon(39.203424);
        tempSight10.setType(1);
        sights.add(tempSight10);

        Sight tempSight11 = new Sight();
        tempSight11.setName("Улица 9 Января");
        tempSight11.setDescription("Улица 9 Января (Большая Девицкая) — улица в Воронеже. На большей части разделяет Советский и Коминтерновский районы Воронежа. Дореволюционное название — Большая Девицкая, которое сейчас продублировано на некоторых домах. Одна из самых протяжённых улиц города (8 км), начинающаяся в центре, около здания областной думы и заканчивающаяся на пересечении с улицей Защитников Родины. После перекрёстка улица переходит в Арбатскую улицу (бывшая ул. 9 января в селе Подклетное), а затем в автодорогу местного значения в сторону Семилук. Имеет минимум две полосы движения в каждую сторону, местами — разделительный барьер. Имеет односторонний участок от Кольцовской улицы до путепровода над железной дорогой Воронеж — Курск[1].\n" +
                "\n" +
                "Нумерация домов от 20 дома и заканчивается 302. Ранее улица начиналась от ВГУ, но 16 января 1990 года часть улицы от государственного университета до улицы Кирова была переименована постановлением горисполкома в улицу Платонова. Нумерацию домов менять не стали.");
        tempSight11.setImagePath("https://image.ibb.co/gHrzqo/home.png");
        tempSight11.setLat(51.673524);
        tempSight11.setLon(39.150417);
        tempSight11.setType(1);
        sights.add(tempSight11);


    }
}


