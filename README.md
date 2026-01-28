# com.hjsoft/project-templates

My collection of project templates for use with:
- https://github.com/seancorfield/deps-new
- https://github.com/practicalli/clojure-cli-config

## Usage

This is a template project for use with [deps-new](https://github.com/seancorfield/deps-new).
As originally generated, it will produce a new library project when run:

    $ clojure -Sdeps '{:deps {com.hjsoft/project-templates {:local/root "."}}}' -Tnew create :template jflinchbaugh/app :name myusername/mycoolapp
