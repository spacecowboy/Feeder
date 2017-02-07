.PHONY: help debian debian-signed debian-clean clean manpages

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


debian: manpages ## Build the debian package
	debuild -b -uc -us

debian-signed: manpages ## Build a signed debian package
	debuild -b

debian-clean: ## Invoke extra cleaning for debian package building
	debuild -- clean

manpages: ## Generate manpages
	mkdir -p docs
	python3 setup.py build_manpage --output=docs
	ls -1 docs/*.1 > debian/feeder.manpages

clean: ## Clean build directories
	rm -rf build
	rm -rf debian/feeder
