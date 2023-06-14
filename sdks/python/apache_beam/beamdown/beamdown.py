from apache_beam.beamdown.pipeline_extension import PipelineExtension
from markdown.core import Markdown, Extension

class Beamdown:
    
    def __init__(self):
        self.context = PipelineExtension()
        self.md = Markdown(extensions=['extra',self.context])
    
    def convert(self,text,args={}):
        self.context.args = args
        return self.md.convert(text)
    
    def reset(self):
        self.md.reset()

    def final_markdown(self):
        return self.md.final_markdown
    
    def pipeline(self):
        return {'pipeline':self.md.pipeline}
    

_beamdown = Beamdown()

def convert(text,args={}):
    return _beamdown.convert(text,args)

def reset():
    _beamdown.reset()

def final_markdown():
    return _beamdown.final_markdown()

