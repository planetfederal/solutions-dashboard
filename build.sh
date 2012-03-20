
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

echo "checking out the twitter css lib thing"
cd ../../
rm -rf bootstrap
curl -O http://twitter.github.com/bootstrap/assets/bootstrap.zip
unzip bootstrap.zip
