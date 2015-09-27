# HAR Generator
This tool enables saving all the content from a given HAR archive file

## How to use
Copy your target har file as well as har.groovy together, then type the following command to generate the website.

    docker run --rm --name groovy-worker \
     -v $PWD:/workspace groovy:2.4.5 \
     bash -c 'cd /workspace && groovy -c utf-8 har.groovy the-target.har'

The tool will generate the complete site to $PWD/output/

## Where to get Docker Image of groovy:2.4.5?
Check https://github.com/stainboy/lean/tree/master/groovy as well as https://github.com/stainboy/lean/tree/master/jre