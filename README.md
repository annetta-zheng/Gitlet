# Gitlet Design Document
author: Annetta Zheng

### 1. Classes and Data Structures
#### Blobs
This class stores a file 
- ##### Fields
1. String _fileid: sha-1 of a file
2. byte[] _bytes: the contents of a file
3. String _name: name of a file
4. String _content: content of a file as a String
5. File _file: the file
#### Branch
This class stores a branch-commits tree of a Git Instance
- ##### Fields
1. String _branch: branch name
2. ArrayList<Commits> _commit: commits chain under the branch
#### Commits
This class stores an instance of a commit holding files (saving the contents of entire directories of files.)
- ##### Fields
1. String _msg: the commit message
2. byte[] _ts: timestamp of the commit
3. String _branch: name of the commit branch
4. TreeMap<String, Blobs> _blobs: commited files in the commit 
5. Commits _parent: the commit's parent (previous commit)
6. Commits _parent2: the commit's second parent (previous commit)
7. String _id: sha-1 of the commit

#### Gitlet
This class stores all the information of a Gitlet repository holding commits
- ##### Fields
1. Commits _head: head of the repository
2. String _curbranch: current branch
3. TreeMap<String, Commits> _allcommits: all commits in the repository
4. TreeMap<String, Blobs> _staged: files staged for addition
5. TreeMap<String, Blobs> _removed: files staged for removal

### 2. Algorithms
#### Main
This class is the driver class for Gitlet that runs Gitlet through Gitlet, Commits, and Blobs. Implements the commands in Algorithms.
1. init
- Creates a new Gitlet version-control system in the current directory.
2. add
- Adds a copy of the file as it currently exists to the staging area
3. commit
- Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
4. rm
- Unstage the file if it is currently staged for addition.
5. log
- Viewing the history of your backups.
  * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.
6. global-log
- Like log, except displays information about all commits ever made.
7. find
- Prints out the ids of all commits that have the given commit message, one per line.
8. status
- Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal.
9. checkout
- Restoring a version of one or more files or entire commits. Takes in FILES and overwrites the version of the FILES
10. branch
- Maintaining related sequences of commits, called branches.
  - Creates a new branch with the given name, and points it at the current head node.
11. rm-branch
- Deletes the branch with the given name.
12. reset
- Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node.
13. merge 
- Merging changes made in one branch into another. Merges files from the given branch into the current branch. 
   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
14. add-remote
- Saves the given login information under the given remote name. Attempts to push or pull from the given remote name will then attempt to use this .gitlet directory.
15. rm-remote
- Remove information associated with the given remote name. The idea here is that if you ever wanted to change a remote that you added, you would have to first remove it and then re-add it.
16. push
- Attempts to append the current branch’s commits to the end of the given branch at the given remote.
17. fetch
- Brings down commits from the remote Gitlet repository into the local Gitlet repository. 
 - Copies all commits and blobs from the given branch in the remote repository into a branch named [remote name]/[remote branch name] in the local .gitlet, changing [remote name]/[remote branch name] to point to the head commit. This branch is created in the local repository if it did not previously exist.
18. pull
- Fetches branch [remote name]/[remote branch name] as for the fetch command, and then merges that fetch into the current branch.

### 3. Persistence
By starting up the program `java gitlet.Main init`, it will create a repo directory and an initial commit through the command init().
Based on the command run, structure of files will be saved.

All other commands:
```
1. java gitlet.Main add [file name]
2. java gitlet.Main commit [message]
3. java gitlet.Main rm [file name]
4. java gitlet.Main log
5. java gitlet.Main global-log
6. java gitlet.Main find [commit message]
7. java gitlet.Main status
8. java gitlet.Main checkout -- [file name]
9. java gitlet.Main checkout [commit id] -- [file name]
10. java gitlet.Main checkout [branch name]
11. java gitlet.Main branch [branch name]
12. java gitlet.Main rm-branch [branch name]
13. java gitlet.Main reset [commit id]
14. java gitlet.Main merge [branch name]
15. java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet
16. java gitlet.Main rm-remote [remote name]
17. java gitlet.Main push [remote name] [remote branch name]
18. java gitlet.Main fetch [remote name] [remote branch name]
19. java gitlet.Main pull [remote name] [remote branch name]
```
