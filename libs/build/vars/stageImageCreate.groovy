def call(app, path, lock_id, built_apps = [:]) {
    return {
        stage(app) {
            lock("Image-create-lock-${lock_id}") {
                dir (path) {
                    sh "docker build -t ${app} ../."
                    built_apps.put(app, path)
                }
            }
        }
    }
}