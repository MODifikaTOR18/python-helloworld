def call(app, path, lock_id) {
    return {
        stage(app) {
            lock("Image-create-lock-${lock_id}") {
                dir (path) {
                    sh "pwd"
                    sh "docker build -t ${app} ../."
                }
            }
        }
    }
}