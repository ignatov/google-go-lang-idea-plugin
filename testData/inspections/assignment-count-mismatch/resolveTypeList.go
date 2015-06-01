package docker2

import "text/template"

func _() (tmpl *template.Template, err error) {
	tmpl, err = template.New("test").Parse("{{.DNS}}")
	return
}
