import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Deduplicator {

    private final Directory idxDir;
    private final float scoreThreshold;

    Deduplicator(List<String> contentList, float scoreThreshold) {
        // Use memory to hold lucene index
        this.idxDir = new RAMDirectory();
        this.scoreThreshold = scoreThreshold;
        // Add all contents to lucene index
        addContents(contentList);
    }

    public List<String> dedup() {
        List<String> results = new ArrayList<String>();
        IndexReader indexReader = null;
        try {
            // Open the index, writable
            indexReader = IndexReader.open(idxDir, false);
            int maxDocNum = indexReader.maxDoc();
            for (int docNum = 0; docNum < maxDocNum; docNum++) {
                if (indexReader.isDeleted(docNum)) continue;

                // Save doc to result list
                Document doc = indexReader.document(docNum);
                String content = getContentFromDoc(doc);
                results.add(content);

                MoreLikeThis moreLikeThis = new MoreLikeThis(indexReader);
                // Lower the frequency since content is short
                moreLikeThis.setMinTermFreq(1);
                moreLikeThis.setMinDocFreq(1);

                // Find Similar doc
                Reader reader = new StringReader(content);
                Query query = moreLikeThis.like(reader, "contents");
                IndexSearcher searcher = new IndexSearcher(indexReader);
                TopDocs topDocs = searcher.search(query, 100);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    if (scoreDoc.score > scoreThreshold) {
                        // Delete all similar docs
                        indexReader.deleteDocument(scoreDoc.doc);
                    }
                }
                closeSearcher(searcher);
            }
            closeIndexReader(indexReader);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
            closeIndexReader(indexReader);
        } catch (IOException e) {
            e.printStackTrace();
            closeIndexReader(indexReader);
        }
        return results;
    }

    private void addContents(List<String> contentList) {
        IndexWriter writer = null;
        try {
            writer = getIndexWriter();
            for (String content : contentList) {
                writer.addDocument(addContentToDoc(content));
            }
            closeIndexWriter(writer);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
            closeIndexWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
            closeIndexWriter(writer);
        }
    }

    private Document addContentToDoc(String content) {
        Document doc = new Document();
        doc.add(new Field("contents", content, Field.Store.YES,
                Field.Index.ANALYZED));
        return doc;
    }

    private String getContentFromDoc(Document doc) {
        return doc.get("contents");
    }

    private IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35));
        return new IndexWriter(idxDir, indexWriterConfig);
    }

    private void closeIndexWriter(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (CorruptIndexException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void closeIndexReader(IndexReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSearcher(IndexSearcher searcher) {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
