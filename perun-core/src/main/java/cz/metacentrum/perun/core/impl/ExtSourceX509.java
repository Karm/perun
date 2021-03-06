/**
 * 
 */
package cz.metacentrum.perun.core.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;

/**
 * Dummy ExtSource - X.508
 * 
 * @author Michal Prochazka michalp@ics.muni.cz
 * @version $Id$
 */
public class ExtSourceX509 extends ExtSource implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceX509.class);
	
	public List<Map<String,String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjects(searchString, 0);
	}
	
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
	  throw new ExtSourceUnsupportedOperationException();
	}
	
	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}
  
  public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }
  
  public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
    throw new ExtSourceUnsupportedOperationException();
  }
}
