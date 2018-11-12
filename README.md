# MULTISENSOR visual-based ontology alignment and GUI

The MULTISENSOR visual-based ontology alignment implements the ontology alignment algorithm for computing a visual-based similarity metric for entity matching between two ontologies. Each ontological
entity is associated with sets of images, retrieved through ImageNet or web-based search, and visual feature extraction, clustering and indexing for computing the similarity between concepts is employed. An adaptation of a popularWordnet-based matching algorithm to exploit the visual similarity has also been developed. More details about this algorithm can be found in [1]. A GUI for the Alignment API is also implemented.

[1] C. Doulaverakis, S. Vrochidis, I. Kompatsiaris, "Exploiting visual similarities for ontology alignment", 7th International Conference on Knowledge Engineering and Ontology Development (KEOD 2015), Lisbon, Portugal, 12-14 November, 2015

#Description

The implementation provided is based on the [Alignment API v4.6](http://alignapi.gforge.inria.fr/). An extension to Alignment API is implemented which allows to combine different ontology matching algorithms using a weighted sum approach where each matcher's score is multiplied before the overall sum is computed. Weights can be set through the method `setMatcherWeights` of the class `gr.iti.multisensor.matrix.MSWeighting`. The Alignment API is not a Maven project so to build it follow the instruction [here](http://alignapi.gforge.inria.fr/maven.html)

For the Visual alignment algorithm, you will have to import the project [multimedia-indexing](https://github.com/MKLab-ITI/multimedia-indexing) for computing feature extraction and indexing. For retrieving the images that correspond to the ontological entities, [ImageNet] (http://www.image-net.org/) and Yahoo Image Search are accessed. For Yahoo Image Search you will have to obtain a BOSS account and provide the Key and Secret in the class `gr.iti.multisensor.ui.utils.Parameters`. You will have to download [Wordnet](https://wordnet.princeton.edu/) and the [Stanford POS tagger](http://nlp.stanford.edu/software/tagger.shtml). For Wordnet, set the appropriate Wordnet dir path in the file `config/config.txt` and in `JWNL_properties.xml`. 

For running the GUI, the main class is `gr.iti.multisensor.ui.MultiAlignMainWindow`

# Version
1.0.0
