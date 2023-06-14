import logging 
from textwrap import dedent
import re
import yaml

from markdown.core import Markdown, Extension
from markdown.preprocessors import Preprocessor
from markdown.extensions.attr_list import get_attrs
from markdown.extensions.codehilite import parse_hl_lines

from jinja2 import Template

LOG = logging.getLogger("BEAMDOWN")

from apache_beam.beamdown.links import Links

class PipelineExtension(Extension):
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
        self.args = {}

    
    def add_chunk(self,id,lang,classes,config,original_text,line_number):
        self.chunk_map[id] = len(self.chunks)
        self.chunks.append({
            '__uuid__': 'beamdown_chunk_{}'.format(len(self.chunks)),
            '__line__': line_number,
            'id': id,
            'lang': lang,
            'classes': classes,
            'config': config,
            'original_text': original_text,
            'final_text': None
        })

    def chunk(self,id = None,index = None):
        if id:
            return self.chunks[self.chunk_map[id]]
        else:
            return self.chunk[index]

class PipelineExtractor(Preprocessor):
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
        text = "\n".join(lines)

        #Tokenize our pipeline components
        tokens = []
        line_count = 1
        while 1:
            m = self.FENCED_BLOCK_RE.search(text)
            #TODO Verify line counts
            if m:
                prefix = text[:m.start()]
                line_count = line_count + prefix.count("\n")
                tokens.append((prefix,m.group('code'),m.group('lang'),m.group('attrs'),m.group('hl_lines'),line_count))
                line_count = line_count + m.group('code').count("\n")
                text = text[m.end():]
            else:
                break
        
        # Do post processing
        chunk_count = 0

        ids = []
        # First we need to register all of our chunks
        for token in tokens:
            # This is the same parsing as the fenced output extension
            lang, id, classes, config, line_number = None, '', [], {}, 0
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
            if config.get('inputs') is not None:
                LOG.debug("INPUTS: {}".format(config['inputs']))
                config['inputs'] = Links(config['inputs'].split(","))
            else:
                config['inputs'] = Links([])

            self.pipeline.add_chunk(id,lang,classes,config,token[1],token[5])
            ids.append(id)
            chunk_count = chunk_count + 1

        # Now pass over each chunk to resolve any macros
        for chunk_id in ids:
            chunk = self.pipeline.chunk(chunk_id)
            template_text = "{}{}".format(self.TEMPLATE_MACROS,chunk['original_text'])
            template = Template(template_text)

            final_text = template.render(
                lang=chunk['lang'],
                config=chunk['config'],
                args=self.pipeline.args)
            chunk['final_text'] = final_text

        #Assemble YAML and final_markdown
        lines = []
        transforms = []
        source = None
        sink = None
        link_count = 0
        for i in range(len(ids)):
            chunk = self.pipeline.chunk(ids[i])
            id, lang, classes, code, config = ids[i],chunk['lang'], chunk['classes'], chunk['final_text'], chunk['config']
            lines.append("{}```{}{}\n```\n".format(
                tokens[i][0],
                chunk['lang'],
                chunk['final_text']
            ))

            #If there is a noexecute class we should not add it to the YAML
            if "noexecute" in classes:
                continue

            transform = None
            
            if lang == 'yaml':
                transform = yaml.safe_load(code)
                input = config['inputs'].input()
                if input is not None:
                    transform['input'] = input
            elif lang == 'sql':
                transform = {'type':'Sql','query':code,'name':id}
                inputs = config['inputs'].inputs()
                if inputs is not None:
                    transform['inputs'] = inputs
            elif lang == 'python':
                type = "PyFn"
                if "filter" in classes:                    
                    transform = {'type':'PyFilter','keep':code}
                    input = config['inputs'].input()
                    if input is not None:
                        transform['input'] = input
                else:
                    LOG.warn("General python functions not yet implemented")

            else:
                LOG.warn("No YAML output available for language {}".format(lang))
            if transform is not None:
                transform['__uuid__'] = chunk['__uuid__']
                transform['__line__'] = chunk['__line__']
                if 'name' not in transform:
                    transform['name'] = id
                if "source" in classes:
                    source = transform
                elif "sink" in classes:
                    sink = transform
                else:
                    transforms.append(transform)
        pipeline = dict()
        if source is not None:
            pipeline['source'] = source
        if sink is not None:
            pipeline['sink'] = sink
        if len(transforms) > 0:
            pipeline['transforms'] = transforms
        self.md.pipeline = pipeline 

        # Cue music...
        self.md.final_markdown = "".join(lines)
        return self.md.final_markdown.split("\n")

