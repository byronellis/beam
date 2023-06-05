import argparse

def _configure_parser(argv):
  parser = argparse.ArgumentParser()
  parser.add_argument('infile',nargs='?',type=argparse.FileType('r'),default=sys.stdin)
  parser.add_argument('outfile',nargs='?',type=argparse.FileType('w'),default=sys.stdout)
  parser.add_argument('--mode',choices=['yaml','markdown'],default='yaml')
  return parser.parse_known_args(argv)

def run(argv=None):
  known_args, pipeline_args = _configure_parser(argv)



if __name__ == '__main__':
  run()

