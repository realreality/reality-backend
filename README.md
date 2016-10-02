# praguehacks-realreality
Quick and dirty server for serving SHP files data build for Real reality project created at [Prague Hacks 2016](http://www.praguehacks.cz/)

Data .shp files has to be converted to [EPSG:3857](http://epsg.io/3857) and column `my_id` (integer increment from 1) has to be added.

You need data files:
* [Bonita klimatu z hlediska znečištění ovzduší](http://opendata.praha.eu/dataset?tags=ovzdu%C5%A1%C3%AD&_tags_limit=0)
* [Stání v zónách placeného stání](http://opendata.praha.eu/dataset?tags=silni%C4%8Dn%C3%AD+doprava&_tags_limit=0)
* [Hluková mapa automobilové dopravy - noc a den](http://opendata.praha.eu/dataset?tags=hluk&_tags_limit=0)
