# PragueHacks - RealReality mock server
Quick and dirty server for serving SHP files data build for Real reality project created at [Prague Hacks 2016](http://www.praguehacks.cz/).

Chrome extension front-end is [here](https://github.com/krtek/reality).

Run it with mvnw spring-boot:run (it's standard [Spring Boot app](https://spring.io/projects/spring-boot))

Data sets below are supported. If there is newer version of data (you can see in metadata of given dataset).
You need to download WSG84 versions of shape files and copy .shp (polygon data), shx (index data) and .dbf (attributes data) files from zip.

Supported data sets:
* [Bonita klimatu z hlediska znečištění ovzduší](http://opendata.praha.eu/dataset/ipr-bonita_klimatu_z_hlediska_znecisteni_ovzdusi)
* [Stání v zónách placeného stání](http://opendata.praha.eu/dataset/ipr-stani_v_zonach_placeneho_stani)
* [Hluková mapa automobilové dopravy - noc](http://opendata.praha.eu/dataset/ipr-hlukova_mapa_automobilove_dopravy_-_noc)
* [Hluková mapa automobilové dopravy - den](http://opendata.praha.eu/dataset/ipr-hlukova_mapa_automobilove_dopravy_-_den)
* [Záplavové území (Q5)](http://opendata.praha.eu/dataset/ipr-zaplavove_uzemi___q5_)
* [Záplavové území (Q20)](http://opendata.praha.eu/dataset/ipr-zaplavove_uzemi___q20_)
* [Záplavové území (Q50)](http://opendata.praha.eu/dataset/ipr-zaplavove_uzemi___q50_)
* [Záplavové území (Q100)](http://opendata.praha.eu/dataset/ipr-zaplavove_uzemi___q100_)
* [Záplavové území (drobné vodní toky)](http://opendata.praha.eu/dataset/ipr-zaplavove_uzemi___drobne_vodni_toky_)

