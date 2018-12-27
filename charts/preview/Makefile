OS := $(shell uname)

preview: 
ifeq ($(OS),Darwin)
	sed -i "" -e "s/version:.*/version: $(PREVIEW_VERSION)/" Chart.yaml
	sed -i "" -e "s/version:.*/version: $(PREVIEW_VERSION)/" ../*/Chart.yaml
	sed -i "" -e "s/tag: .*/tag: $(PREVIEW_VERSION)/" values.yaml
else ifeq ($(OS),Linux)
	sed -i -e "s/version:.*/version: $(PREVIEW_VERSION)/" Chart.yaml
	sed -i -e "s/version:.*/version: $(PREVIEW_VERSION)/" ../*/Chart.yaml
	sed -i -e "s|repository: .*|repository: $(DOCKER_REGISTRY)\/nuxeo-sandbox\/jenkinsx-quickstart-nuxeo-poc|" values.yaml
	sed -i -e "s/tag: .*/tag: $(PREVIEW_VERSION)/" values.yaml
else
	echo "platfrom $(OS) not supported to release from"
	exit -1
endif
	echo "    version: $(PREVIEW_VERSION)" >> requirements.yaml
	sed -e '1d;' requirements.yaml >> requirements.yaml.tmp
	cat ../jenkinsx-quickstart-nuxeo-poc/requirements.yaml > requirements.yaml
	echo '' >> requirements.yaml
	cat requirements.yaml.tmp >> requirements.yaml
	jx step helm build
