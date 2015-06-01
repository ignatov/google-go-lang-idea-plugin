package docker4

import "net/http"

func _(r *http.Request) bool {
	if _, ok := r.Form["demo"]; !ok {
		return false
	}
	return false
}

