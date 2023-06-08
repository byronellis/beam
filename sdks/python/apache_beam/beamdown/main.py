import argparse
import sys
import logging

from apache_beam.beamdown import Beamdown

LOG = logging.getLogger("BeamdownMain")

def _parse_cmdline(argv):
  parser = argparse.ArgumentParser()
  parser.add_argument('infile',nargs='?',type=argparse.FileType('r'),default=sys.stdin,help="Beamdown file to process")
  parser.add_argument('outfile',nargs='?',type=argparse.FileType('w'),default=sys.stdout,help="Output file in either markdown or yaml depend on the mode.")
  parser.add_argument('--mode',choices=['yaml','markdown'],default='yaml')
  return parser.parse_known_args(argv)

def run(argv=None):
  logging.basicConfig(level=logging.DEBUG)
  known_args, pipeline_args = _parse_cmdline(argv)
  content = known_args.infile.read()

  beamdown = Beamdown()
  beamdown.convert(content)
#  known_args.outfile.write(beamdown.convert(content))
  known_args.outfile.write("\n")
  for pipeline in beamdown.pipelines():
    LOG.debug("{}".format(pipeline))





if __name__ == '__main__':
  run()

