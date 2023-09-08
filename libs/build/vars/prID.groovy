def call(branch) {
    if (branch =~ /PR-*/) {
        pr_id = env.CHANGE_ID
        println ("Changes in branch: ${env.CHANGE_ID}")
    } else {
        try {
            commit_name = sh(script: 'git show -s --format=%s', returnStdout: true).trim()
            pr_id = commit_name.substring(commit_name.indexOf('#')+1, commit_name.indexOf(' from'))
            println ("Changes in current branch ${branch}: ${pr_id}")
        } catch(err) {
            println ("Failed: ${err.getMessage()}")
            pr_id = ''
        }
    }
    return pr_id
}