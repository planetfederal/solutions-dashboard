
echo "Removing old javascript libs"
rm -r resources/public/js/libs
mkdir -p resources/public/js/libs

echo "Checking out the new javascript libs"
cd  resources/public/js/libs

echo "jquery"
curl -XGET http://code.jquery.com/jquery-1.7.1.min.js >> jquery.min.js

echo "underscore"
curl -XGET http://documentcloud.github.com/underscore/underscore-min.js >> underscore.min.js

echo "backbone"
curl -XGET http://documentcloud.github.com/backbone/backbone-min.js >> backbone.min.js

echo "slickgrid"
git clone https://github.com/mleibman/SlickGrid.git slickgrid
