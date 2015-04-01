/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.pcslabinterface.db;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.ConceptNumeric;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.pcslabinterface.PcsLabInterfaceDAO;

/**
 * @author jkeiper
 */
public class HibernatePcsLabInterfaceDAO implements PcsLabInterfaceDAO {
     private static Log log = LogFactory.getLog(HibernatePcsLabInterfaceDAO.class);
	/**
	 * the session factory to use in this DAO
	 */
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<Integer> getNumericConceptIds() {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(ConceptNumeric.class)
				.setProjection(Projections.id());
		return crit.list();
	}

	public Provider getProviderBySystemId(String systemId) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class)
				.add(Restrictions.eq("systemId", systemId))
				.setProjection(Projections.property("person"));

        Person person = (Person)crit.uniqueResult();
        Provider provider = null;
        //Confirm if there is a provider associated with this person

        if(person != null) {
            try {
                List<Provider> providers = (List<Provider>)Context.getProviderService().getProvidersByPerson(person);

                //If it exists return the first provider
                if(providers!=null && !providers.isEmpty()){
                    provider = providers.get(0);
                }

            }catch (IllegalArgumentException ie) {
                log.debug(ie.getMessage());
            }
        }
        return provider;
	}

}
