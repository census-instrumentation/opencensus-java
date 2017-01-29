import os
import sys
import traceback

def main(argv):
  try:
    # Only check the history if the build is running on a pull request. Travis
    # always merges pull requests into the base branch before running the build.
    if is_merged_pull_request():
      # These functions assume that HEAD^1 is the base branch and HEAD^2 is the
      # pull request.
      exit_if_pull_request_has_merge_commits()
      exit_if_pull_request_does_not_start_from_base_branch()
      print 'Checked pull request history.'
    else:
      print 'Skipped history check.'
  except Exception as e:
    # Don't stop the build if this script has a bug.
    traceback.print_exc(e)

def is_merged_pull_request():
    '''Returns true if the current commit is a merge between a pull request and
       an existing branch.'''
    # When Travis merges a pull request, the commit has no branches pointing to
    # it, and the only decoration is HEAD.
    return read_process('git show --no-patch --format="%D"').strip() == 'HEAD'

def exit_if_pull_request_has_merge_commits():
  '''Exits with an error if any of the commits added by the pull request are
     merge commits.'''
  # Print the parents of each commit added by the pull request.
  git_command = 'git log --format="%P" HEAD^1..HEAD^2'
  for line in os.popen(git_command):
    parents = line.split()
    assert len(parents) >= 1, line
    if len(parents) > 1:
      print 'Pull request contains a merge commit:'
      print_history()
      sys.exit(1)

def exit_if_pull_request_does_not_start_from_base_branch():
  '''Exits with an error if the pull request is not branched from a commit on
     the base branch.'''
  start = get_commit(list_pull_request_commits()[-1] + '^')
  if not start in list_commits('HEAD^1'):
    print 'Pull request does not start from the base branch:'
    print_history()
    sys.exit(1)

def list_pull_request_commits():
  '''Returns a list of all commit hashes that are contained in the pull
     request but not the base branch.'''
  return get_log_commits('git log --format="%H" HEAD^1..HEAD^2')

def list_commits(branch):
  '''Returns a list of all commit hashes on the given branch, following only
     the first parent.'''
  return get_log_commits('git log --first-parent --format="%H" ' + branch)

def get_log_commits(git_log_command):
  commits = []
  for line in os.popen(git_log_command):
    commit = line.strip()
    assert_commit(commit)
    commits.append(commit)
  return commits

def get_commit(revision):
  git_command = 'git show --no-patch --format="%H" ' + revision
  commit = read_process(git_command).strip()
  assert_commit(commit)
  return commit

def assert_commit(commit):
  '''Assert that the string has the format of a git commit hash.'''
  assert set(commit) <= set('abcdef0123456789'), commit
  assert len(commit) == 40, commit

def print_history():
  os.system('git log HEAD^1 HEAD^2 -30 --graph --oneline --decorate')

def read_process(command):
  '''Runs a command and returns everything printed to stdout.'''
  with os.popen(command, 'r') as fd:
    return fd.read()

if __name__ == '__main__':
  main(sys.argv)
