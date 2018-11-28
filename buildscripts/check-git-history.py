import os
import sys
import traceback

def main(argv):
  # Only check the history if the build is running on a pull request.
  # Build could be running on pull request using travis or kokoro.
  if is_travis_pull_request() or is_kokoro_presubmit_request():
    # This function assumes that HEAD^1 is the base branch and HEAD^2 is the
    # pull request.
    exit_if_pull_request_has_merge_commits()
    print 'Checked pull request history: SUCCEEDED'
  else:
    print 'Skipped history check.'

def is_kokoro_presubmit_request():
  '''Returns true if KOKORO_GITHUB_PULL_REQUEST_NUMBER is set.'''
  if 'KOKORO_GITHUB_PULL_REQUEST_NUMBER' in os.environ:
    return True
  return False

def is_travis_pull_request():
  '''Returns true if TRAVIS_PULL_REQUEST is set to indicate a pull request.'''
  if 'TRAVIS_PULL_REQUEST' in os.environ:
    return os.environ['TRAVIS_PULL_REQUEST'] != 'false'
  return False

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
      print 'Checked pull request history: FAILED'
      sys.exit(1)

def print_history():
  os.system('git log HEAD^1 HEAD^2 -30 --graph --oneline --decorate')

def read_process(command):
  '''Runs a command and returns everything printed to stdout.'''
  with os.popen(command, 'r') as fd:
    return fd.read()

if __name__ == '__main__':
  main(sys.argv)
