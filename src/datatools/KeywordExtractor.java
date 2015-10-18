package datatools;

import java.util.ArrayList;
import java.util.List;

import at.jku.faw.keywords.KeywordCandidate;
import at.jku.faw.keywords.KeywordExtractorFactory;
import at.jku.faw.keywords.Lang;
import at.jku.faw.keywords.properties.KeywordExtractorProperties;
import at.jku.faw.keywords.properties.StopwordProperties;
import at.jku.faw.keywords.properties.SynonymFinderFactoryProperties;
import at.jku.faw.keywords.properties.WordnetProperties;
import at.jku.faw.keywords.simple.SimpleKeywordExtractor;

/**
 * @author hkosorus
 *
 */
public class KeywordExtractor {

	private SimpleKeywordExtractor keywordExtractor;

	public KeywordExtractor(String posTaggerModelPath, String stopwordPath,
			String wordnetPath, String wordnetVersion, String openThesaurusUrl,
			String openThesaurusUser, String openThesaurusPass) {
		KeywordExtractorProperties properties = KeywordExtractorProperties
				.getInstance();
		properties.setPosTaggerModelPath(posTaggerModelPath);

		StopwordProperties stopwordProperties = new StopwordProperties();
		stopwordProperties.setPath(stopwordPath);
		properties.setStopwordProperties(stopwordProperties);

		WordnetProperties wordnetProperties = new WordnetProperties();
		wordnetProperties.setPath(wordnetPath);
		wordnetProperties.setVersion(wordnetVersion);
		properties.setWordnetProperties(wordnetProperties);

		SynonymFinderFactoryProperties synonymProperties = new SynonymFinderFactoryProperties();
		synonymProperties.setDatabaseUrl(openThesaurusUrl);
		synonymProperties.setDatabaseUser(openThesaurusUser);
		synonymProperties.setDatabasePassword(openThesaurusPass);
		synonymProperties.setAllowedLevels(new String[] { "5", "6" });
		synonymProperties.setAllowedCategories(new String[] { "4", "5", "9",
				"32" });
		properties.setSynonymFinderFactoryProperties(synonymProperties);

		keywordExtractor = KeywordExtractorFactory
				.getSimpleKeywordExtractor(properties);
	}

	ArrayList<String> extractStemsFromText(String text, int depth,
			boolean singlewordconcepts, Lang lang, boolean caseSensitive) {

		ArrayList<String> result = new ArrayList<String>();

		List<KeywordCandidate> list = keywordExtractor
				.extractKeywordSequenceFromText(text, depth,
						singlewordconcepts, lang, caseSensitive, "");

		for (KeywordCandidate k : list) {
			result.add(k.getStemmed());
		}

		return result;

	}

	ArrayList<String> extractBaseWordsFromText(String text, int depth,
			boolean singlewordconcepts, Lang lang, boolean caseSensitive) {

		ArrayList<String> result = new ArrayList<String>();

		List<KeywordCandidate> list = keywordExtractor
				.extractKeywordSequenceFromText(text, depth,
						singlewordconcepts, lang, caseSensitive, "");

		for (KeywordCandidate k : list) {
			result.add(k.getBaseform());
		}

		return result;

	}

	List<KeywordCandidate> extractKeywordCandidatesFromText(String text,
			int depth, boolean singlewordconcepts, Lang lang,
			boolean caseSensitive) {

		return keywordExtractor.extractKeywordSequenceFromText(text, depth,
				singlewordconcepts, lang, caseSensitive, "");

	}

}
