/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.pcslabinterface.db;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.ConceptNumeric;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceDAO;

/**
 *
 * @author jkeiper
 */
public class HibernatePcsLabInterfaceDAO implements PcsLabInterfaceDAO {

	/**
	 * the session factory to use in this DAO
	 */
	private SessionFactory sessionFactory;

	/**
	 * @see DataIntegrityDAO#setSessionFactory(SessionFactory)
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<Integer> getNumericConceptIds() {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptNumeric.class)
				.setProjection(Projections.id());
		return crit.list();
	}
	
}
