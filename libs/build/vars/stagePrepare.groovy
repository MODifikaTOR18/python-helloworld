def call(flag, apps, parallel_count, built_apps = [:]) {
    stageList = []
    stageMap = [:]
    apps.eachWithIndex { app, path, i ->
        Integer lock_id = i % parallel_count
        stageMap.put(app, stageImageCreate(app, path, lock_id, built_apps))
    }
    stageList.add(stageMap)
    return stageList
}