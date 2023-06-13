import argparse
import sys
import logging
import yaml
import re

from apache_beam.beamdown import Beamdown

LOG = logging.getLogger("BeamdownMain")

def _parse_cmdline(argv):
  parser = argparse.ArgumentParser()
  parser.add_argument('infile',nargs='?',type=argparse.FileType('r'),default=sys.stdin,help="Beamdown file to process")
  parser.add_argument('outfile',nargs='?',type=argparse.FileType('w'),default=sys.stdout,help="Output file in either markdown or yaml depend on the mode.")
  parser.add_argument('--mode',choices=['yaml','markdown'],default='yaml')
  parser.add_argument('--param',nargs='*',action="append",help="Arguments to pass to the Beamdown processor")
  return parser.parse_known_args(argv)

def _double_quote_handler(scanner,text):
  key,value = text.split('=',1)
  return key, value.strip('"')

def _single_quote_handler(scanner,text):
  key,value = text.split('=',1)
  return key, value.string("'")

def _no_quote_handler(scanner,text):
  return text.split('=',1)

def _bare_word_handler(scanner,text):
  return text,True


def run(argv=None):
  logging.basicConfig(level=logging.DEBUG)
  known_args, pipeline_args = _parse_cmdline(argv)
  content = known_args.infile.read()


  scanner = re.Scanner([
    (r'[^=]+=".*?"]',_double_quote_handler),
    (r"[^=]+='.*?']",_single_quote_handler),
    (r'[^=]+=[^ =]+',_no_quote_handler),
    (r'[^=]+',_bare_word_handler),
    (r' ',None)
  ])
  args = dict()
  if known_args.param is not None:
    LOG.debug("PARAMS {}".format(known_args.param))
    for param in known_args.param:
      for parsed_param in scanner.scan(param[0])[0]:
        key,value = parsed_param
        args[key] = value
  LOG.debug("Got arguments {}".format(args))


  beamdown = Beamdown()
  beamdown.convert(content,args)
  if known_args.mode == "markdown":
    known_args.outfile.write(beamdown.final_markdown())
  elif known_args.mode == "yaml":
    known_args.outfile.write(yaml.dump(beamdown.pipeline()))
  else:
    LOG.error("Unrecognized mode: {}".format(known_args.mode))
  known_args.outfile.write("\n")





if __name__ == '__main__':
  run()

