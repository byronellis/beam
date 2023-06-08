""" Markdown literate programming extension for YAML """

import logging 
from textwrap import dedent
import re


import markdown
from markdown.core import Markdown
from markdown.extensions.attr_list import get_attrs
from markdown.extensions.codehilite import parse_hl_lines

from jinja2 import Environment, PackageLoader, select_autoescape

LOG = logging.getLogger("BEAMDOWN")

class PipelineExtension(markdown.Extension):
    """ Pipeline extension"""

    def __init__(self,**kwargs):
        super().__init__(**kwargs)
        self.chunks = []
    
    def extendMarkdown(self, md: Markdown) -> None:
        # Try to make sure this runs before other tree processors like codehilite so we can 
        # extract the actual code
        md.preprocessors.register(PipelineExtractor(md,self),'pipeline',40)
        md.registerExtension(self)

        for preprocessor in md.preprocessors:
            LOG.debug("{}".format(preprocessor))

    def reset(self):
        LOG.debug("Resetting pipeline definition")
    
    def add_chunk(self,id,lang,classes,config,original_text):
        self.chunks.append({
            id: id,
            lang: lang,
            classes: classes,
            config: config,
            original_text: original_text
        })


class PipelineExtractor(markdown.preprocessors.Preprocessor):
    """ Extract pipelines from the code block elements """

    FENCED_BLOCK_RE = re.compile(dedent(r'''
    (?P<fence>^(?:~{3,}|`{3,}))[ ]*                          # opening fence
    ((\{(?P<attrs>[^\}\n]*)\})|                              # (optional {attrs} or
    (\.?(?P<lang>[\w#.+-]*)[ ]*)?                            # optional (.)lang
    (hl_lines=(?P<quot>"|')(?P<hl_lines>.*?)(?P=quot)[ ]*)?) # optional hl_lines)
    \n                                                       # newline (end of opening fence)
    (?P<code>.*?)(?<=\n)                                     # the code block
    (?P=fence)[ ]*$                                          # closing fence
    '''),re.MULTILINE | re.DOTALL | re.VERBOSE)

    def __init__(self, md, pipeline):
        super().__init__(md)        
        self.pipeline = pipeline

    def run(self,lines):
        self.md.pipelines = []
        text = "\n".join(lines)

        #Tokenize our pipeline components
        tokens = []
        while 1:
            m = self.FENCED_BLOCK_RE.search(text)
            if m:
                tokens.append((text[:m.start()],m.group('code'),m.group('lang'),m.group('attrs'),m.group('hl_lines')))
                text = text[m.end():]
            else:
                break
        
        # Do post processing
        chunk_count = 0

        ids = []
        # First we need to register all of our chunks
        for token in tokens:
            # This is the same parsing as the fenced output extension
            lang, id, classes, config = None, '', [], {}
            if token[3]:
                for k,v in get_attrs(token[3]):
                    if k == 'id':
                        id = v
                    elif k == '.':
                        classes.append(v)
                    elif k == 'hl_lines':
                        config['hl_lines'] = parse_hl_lines(v)
                    else:
                        config[k] = v
                if len(classes):
                    lang = classes.pop(0)
            else:
                if token[2]:
                    lang = token[2]
                if token[4]:
                    config['hl_lines'] = parse_hl_lines(token[4])
            if not id:
                id = "chunk{}".format(chunk_count+1)
            self.pipeline.add_chunk(id,lang,classes,config,token[1])
            ids.append(id)
            chunk_count = chunk_count + 1

        # Now assemble pipelines from these chunks
        



        # TODO: Replace with post-processed output to allow further correct processing
        return lines



class Beamdown:
    def __init__(self):
        self.md = markdown.Markdown(extensions=['extra',PipelineExtension()])
    
    def convert(self,text):
        return self.md.convert(text)
    
    def reset(self):
        self.md.reset()

    def pipelines(self):
        return self.md.pipelines
    

_beamdown = Beamdown()

def convert(text):
    return _beamdown.convert(text)

def reset():
    _beamdown.reset()

def pipelines():
    return _beamdown.pipelines()
