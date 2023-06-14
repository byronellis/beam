from logging import Logger

LOG = Logger("BEAMDOWN")

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
    
    def input(self):
        if len(self.links) > 0:
            return self.links[0]
        else:
            None
    
    def inputs(self):
        LOG.debug("{} Links for input: {} ".format(len(self.links),self.links))
        if len(self.links) > 0:
            inputs = dict()
            for i in range(len(self.links)):
                LOG.debug("LINK {}".format(i))
                inputs["input{}".format(i)] = self.links[i]
            return inputs
        else:
            return None
        
    def count(self):
        return len(self.links)
