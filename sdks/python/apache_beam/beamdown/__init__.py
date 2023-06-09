""" Markdown literate programming extension for YAML """

import logging 
from textwrap import dedent
import re
import yaml


import markdown
from markdown.core import Markdown
from markdown.extensions.attr_list import get_attrs
from markdown.extensions.codehilite import parse_hl_lines

from jinja2 import Template

LOG = logging.getLogger("BEAMDOWN")

class Links:
    """ Keep track of links between chunks """
    def __init__(self,links):
        self.links = links

    def link(self,ref):
        try:
            ndx = self.links.index(ref)
            return "input{}".format(ndx)
        except:
            ndx = len(self.links)
            self.links.append(ref)
            return "input{}".format(ndx)

class PipelineExtension(markdown.Extension):
    """ Pipeline extension"""

    def __init__(self,**kwargs):
        super().__init__(**kwargs)
        self.reset()
    
    def extendMarkdown(self, md: Markdown) -> None:
        # Try to make sure this runs before other tree processors like codehilite so we can 
        # extract the actual code
        md.preprocessors.register(PipelineExtractor(md,self),'pipeline',40)
        md.registerExtension(self)

        for preprocessor in md.preprocessors:
            LOG.debug("{}".format(preprocessor))

    def reset(self):
        LOG.debug("Resetting pipeline definition")
        self.chunks = []
        self.chunk_map = dict()

    
    def add_chunk(self,id,lang,classes,config,original_text):
        self.chunk_map[id] = len(self.chunks)
        self.chunks.append({
            'id': id,
            'lang': lang,
            'classes': classes,
            'config': config,
            'original_text': original_text
        })

    def chunk(self,id = None,index = None):
        if id:
            return self.chunks[self.chunk_map[id]]
        else:
            return self.chunk[index]




class PipelineExtractor(markdown.preprocessors.Preprocessor):
    """ Extract pipelines from the code block elements """

    TEMPLATE_MACROS="""
{% macro ref(chunk_id) -%}
{{ config.inputs.link(chunk_id) }}
{%- endmacro %}"""


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
            
            #Convert inputs and outputs
            config['inputs'] = Links(config.get('inputs',[]))

            self.pipeline.add_chunk(id,lang,classes,config,token[1])
            ids.append(id)
            chunk_count = chunk_count + 1

        # Now assemble pipelines from these chunks
        for chunk_id in ids:
            chunk = self.pipeline.chunk(chunk_id)
            template_text = "{}{}".format(self.TEMPLATE_MACROS,chunk['original_text'])
            template = Template(template_text)

            final_text = template.render(
                lang=chunk['lang'],
                config=chunk['config'],
                args={})
            LOG.debug("output text {}".format(final_text))



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
