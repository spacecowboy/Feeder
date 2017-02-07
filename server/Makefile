.PHONY: help debian install build lib clean

BIN=${DESTDIR}/usr/bin/feeder
PYSCRIPTS=$(patsubst scripts/%.py,${DESTDIR}/usr/share/feeder/scripts/%.py,$(shell find scripts -name '*.py'))
#SERVICES=$(patsubst services/%,${DESTDIR}/lib/systemd/system/%,$(shell find services -name '*'))

LIBFILES=$(patsubst feeder/%.py,build/lib/feeder/%.py,$(shell find feeder -name '*.py'))
LIB=$(patsubst feeder/%.py,$(DESTDIR)/usr/lib/python3/dist-packages/feeder/%.py,$(shell find feeder -name '*.py'))

build: ## Build the python library

install: ${PYSCRIPTS} ${BIN} ${LIB} ## Install
	python3 setup.py install --install-layout=deb --prefix=${DESTDIR}/usr

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

debian: ## Build the debian package
	# Sign it: debuild -b --lintian-opts --profile debian
	debuild -b -uc -us --lintian-opts --profile debian

clean: ## Clean build directories
	rm -rf build
	rm -rf debian/feeder
	python setup.py clean

build/lib/feeder/%.py: feeder/%.py
	python3 setup.py build

$(DESTDIR)/usr/share/feeder/scripts/%: scripts/%
	mkdir -p $(dir $@)
	cp $< $@

$(DESTDIR)/usr/lib/python3/dist-packages/feeder/%: build/lib/feeder/%
	mkdir -p $(dir $@)
	cp $< $@

$(DESTDIR)/lib/systemd/system/%: services/%
	mkdir -p $(dir $@)
	cp $< $@

$(DESTDIR)/usr/bin/%: bin/%
	mkdir -p $(dir $@)
	cp $< $@
