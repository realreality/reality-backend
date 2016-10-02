# PragueHacks - RealReality mock server
Quick and dirty server for serving SHP files data build for Real reality project created at [Prague Hacks 2016](http://www.praguehacks.cz/)

Data .shp files has to be converted to [EPSG:3857](http://epsg.io/3857) and column `my_id` (integer increment from 1) has to be added.

You need data files:
* [Bonita klimatu z hlediska znečištění ovzduší](http://opendata.praha.eu/dataset?tags=ovzdu%C5%A1%C3%AD&_tags_limit=0)
* [Stání v zónách placeného stání](http://opendata.praha.eu/dataset?tags=silni%C4%8Dn%C3%AD+doprava&_tags_limit=0)
* [Hluková mapa automobilové dopravy - noc a den](http://opendata.praha.eu/dataset?tags=hluk&_tags_limit=0)
* [Cenová mapa Hl. M. Prahy - Plochy](http://www.geoportalpraha.cz/cs/fulltext_geoportal?id=C4FE893C-81B9-4B4A-BDB4-292479C87E2D#.V_DaNI-LRD8)

## How to prepare data
1. Download [QGIS](http://www.qgis.org/)
2. Open .shp file in QGIS
3. Click "Add Vector layer"
4. Source type: File, Click Browse and select .shp file
5. At "Layer panel" right click on particular Layer and select "Save as..."
6. Browse for "Save as" file input box. Select CRS "EPSG:3857 - WGS 84 / Pseudo Mercator" and click Ok
7. New converted layer will be added to "Layer panel"
8. Right click on new layer at "Layyer panel" and select "Edit attributes table"
9. Click on "Toggle editing mode" button (first) and than click "Open field calculator" button
10. Keep "Create new field" checked, set "Output field name" to value "my_id", set "Output field type" to integer, and set "Expression" to value "$id+1"
11. Press Ok and click on "Toggle editing mode" button. You will be promted to save data. Answer yes :]
12. Now you have new .shp file prepared
