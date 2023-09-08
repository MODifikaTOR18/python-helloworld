def call(diff, path) {
    for (file in diff) {
        if (file =~ path) {
            return true
        }
    }
}