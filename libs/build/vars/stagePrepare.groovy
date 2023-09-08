def call(flag, apps, parallel_count, built_apps = [:]) {
    stageList = []
    stageMap = [:]
    apps.eachWithIndex { app, path, i ->
        Integer lock_id = i % parallel_count
        // if (flag == 'build') {
        //     stageMap.put(app, stageBuildCreate(app, path, lock_id))
        // } else {
        stageMap.put(app, stageImageCreate(app, path, lock_id))
        // }
    }
    stageList.add(stageMap)
    return stageList
}