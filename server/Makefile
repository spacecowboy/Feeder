.PHONY: help debian debian-signed debian-clean clean

help: ## Print this help text
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'


debian: ## Build the debian package
	debuild -b -uc -us

debian-signed: ## Build a signed debian package
	debuild -b

debian-clean: ## Invoke extra cleaning for debian package building
	debuild -- clean

clean: ## Clean build directories
	rm -rf build
	rm -rf debian/feeder
