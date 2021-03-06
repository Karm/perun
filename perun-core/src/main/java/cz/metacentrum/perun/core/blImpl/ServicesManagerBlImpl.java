package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ServicesManagerImplApi;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 * @version $Id$
 */
public class ServicesManagerBlImpl implements ServicesManagerBl {

  final static Logger log = LoggerFactory.getLogger(ServicesManagerBlImpl.class);

  private ServicesManagerImplApi servicesManagerImpl;
  private PerunBl perunBl;

  public ServicesManagerBlImpl(ServicesManagerImplApi servicesManagerImpl) {
    this.servicesManagerImpl = servicesManagerImpl;
  }

  public Service createService(PerunSession sess, Service service, Owner owner) throws InternalErrorException, ServiceExistsException {
    //check if service with same name exists in perun
    try {
      Service s = getServicesManagerImpl().getServiceByName(sess, service.getName());
      throw new ServiceExistsException(s);
    } catch(ServiceNotExistsException ex) { /* OK */ }

    getPerunBl().getAuditer().log(sess, "{} created. {}.", service, owner);
    return getServicesManagerImpl().createService(sess, service, owner);
  }

  public void deleteService(PerunSession sess, Service service) throws InternalErrorException, RelationExistsException, ServiceAlreadyRemovedException {
    // Check if the relation with the resources exists
    if (this.getAssignedResources(sess, service).size() > 0) {
      throw new RelationExistsException("Service is defined on some resource");
    }

    getServicesManagerImpl().removeAllRequiredAttributes(sess, service);
    getServicesManagerImpl().deleteService(sess, service);
    getPerunBl().getAuditer().log(sess, "{} deleted.", service);
  }

  public void updateService(PerunSession sess, Service service) throws InternalErrorException {
    Utils.notNull(service.getName(), "service.name");
    getServicesManagerImpl().updateService(sess, service);
    getPerunBl().getAuditer().log(sess, "{} updated.", service);
  }

  public Service getServiceById(PerunSession sess, int id) throws InternalErrorException, ServiceNotExistsException {
    return getServicesManagerImpl().getServiceById(sess, id);
  }
  
  public Service getServiceByName(PerunSession sess, String name) throws InternalErrorException, ServiceNotExistsException {
    return getServicesManagerImpl().getServiceByName(sess, name);
  }

  public List<Service> getServices(PerunSession sess) throws InternalErrorException {
    return getServicesManagerImpl().getServices(sess);
  }
  
  public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
    return getServicesManagerImpl().getServicesByAttributeDefinition(sess, attributeDefinition);
  }

  public List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException {
    return getServicesManagerImpl().getAssignedResources(sess, service);
  }

  private ServiceAttributes getData(PerunSession sess, Service service, Resource resource) throws InternalErrorException {
    ServiceAttributes resourceServiceAttributes = new ServiceAttributes();
    resourceServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));

    List<Member> members;
    members = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
    for (Member member: members) {
      resourceServiceAttributes.addChildElement(getData(sess, service, resource, member));
    }

    return resourceServiceAttributes; 

  }

  private ServiceAttributes getData(PerunSession sess, Service service, Facility facility, Resource resource) throws InternalErrorException {
    ServiceAttributes resourceServiceAttributes = new ServiceAttributes();
    resourceServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));

    List<Member> members;
    members = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
    for (Member member: members) {
      resourceServiceAttributes.addChildElement(getData(sess, service, facility, resource, member));
    }

    return resourceServiceAttributes; 

  }

  private ServiceAttributes getDataWithGroups(PerunSession sess, Service service, Facility facility, Resource resource) throws InternalErrorException {
    ServiceAttributes resourceServiceAttributes = new ServiceAttributes();
    resourceServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));

    ServiceAttributes membersAbstractSA = new ServiceAttributes();
    Map<Member, ServiceAttributes> memberAttributes = new HashMap<Member, ServiceAttributes>();
    List<Member> members = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
    for(Member member : members) {
      ServiceAttributes tmpAttributes = getData(sess, service, facility, resource, member);
      memberAttributes.put(member, tmpAttributes);
      membersAbstractSA.addChildElement(tmpAttributes);
    }

    ServiceAttributes groupsAbstractSA = new ServiceAttributes();
    List<Group> groups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
    for(Group group: groups) {
      groupsAbstractSA.addChildElement(getData(sess, service, facility, resource, group, memberAttributes));
    }

    //assign abstract services attributes to resource service attributes
    resourceServiceAttributes.addChildElement(groupsAbstractSA);
    resourceServiceAttributes.addChildElement(membersAbstractSA);

    return resourceServiceAttributes; 
  }

  private ServiceAttributes getData(PerunSession sess, Service service, Facility facility, Resource resource, Group group, Map<Member, ServiceAttributes> memberAttributes) throws InternalErrorException {
    ServiceAttributes groupServiceAttributes = new ServiceAttributes();
    try {
        groupServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group, true));
    } catch (WrongAttributeAssignmentException ex) {
        throw new InternalErrorException(ex);
    }
    ServiceAttributes groupsSubGroupsElement = new ServiceAttributes();
    // FIXME Do not get subgroups of the members group
    if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
      List<Group> subGroups = getPerunBl().getGroupsManagerBl().getSubGroups(sess, group);
      for(Group subGroup : subGroups) {
        groupsSubGroupsElement.addChildElement(getData(sess, service, facility, resource, subGroup, memberAttributes));
      }
    }

    ServiceAttributes groupsMembersElement = new ServiceAttributes();
    List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembersExceptInvalid(sess, group);
    for(Member member : members) {
      groupsMembersElement.addChildElement(memberAttributes.get(member));
    }

    groupServiceAttributes.addChildElement(groupsSubGroupsElement);
    groupServiceAttributes.addChildElement(groupsMembersElement);
    return groupServiceAttributes;
  }

  private ServiceAttributes getData(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException {
    ServiceAttributes memberServiceAttributes = new ServiceAttributes();
    try {
        memberServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource, member, true));
    } catch(WrongAttributeAssignmentException ex) {
        throw new InternalErrorException(ex);
    }
    return memberServiceAttributes;
  }

  private ServiceAttributes getData(PerunSession sess, Service service, Facility facility, Resource resource, Member member) throws InternalErrorException {
    ServiceAttributes memberServiceAttributes = new ServiceAttributes();

    User user;
    try {
      user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
      memberServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility, resource, user, member));
    } catch (UserNotExistsException e) {
      throw new ConsistencyErrorException("Member has assigned non-existing user.", e);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
    
    return memberServiceAttributes;
  }

  public ServiceAttributes getHierarchicalData(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
    ServiceAttributes serviceAttributes = new ServiceAttributes();
    serviceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility));

    List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
    resources.retainAll(getAssignedResources(sess, service));
    for(Resource resource: resources) {
      ServiceAttributes resourceServiceAttributes = getData(sess, service, facility, resource);
      serviceAttributes.addChildElement(resourceServiceAttributes); 
    }
    return serviceAttributes;
  }

  public ServiceAttributes getFlatData(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
    ServiceAttributes serviceAttributes = new ServiceAttributes();
    serviceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility));


    ServiceAttributes allResourcesServiceAttributes = new ServiceAttributes();
    List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
    for(Resource resource : facilityResources) {
      ServiceAttributes resourceServiceAttributes = new ServiceAttributes();
      resourceServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, resource));
      allResourcesServiceAttributes.addChildElement(resourceServiceAttributes);
    }

    ServiceAttributes allUsersServiceAttributes = new ServiceAttributes();
    List<User> facilityUsers = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
    for (User user : facilityUsers) {
      ServiceAttributes userServiceAttributes = new ServiceAttributes();
      userServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, user));
      userServiceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility, user));
      allUsersServiceAttributes.addChildElement(userServiceAttributes);
    }

    serviceAttributes.addChildElement(allResourcesServiceAttributes);
    serviceAttributes.addChildElement(allUsersServiceAttributes);

    return serviceAttributes; 


  }

  public ServiceAttributes getDataWithGroups(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
    ServiceAttributes serviceAttributes = new ServiceAttributes();
    serviceAttributes.addAttributes(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, service, facility));

    List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
    resources.retainAll(getAssignedResources(sess, service));
    for(Resource resource: resources) {
      ServiceAttributes resourceServiceAttributes = getDataWithGroups(sess, service, facility, resource);
      serviceAttributes.addChildElement(resourceServiceAttributes); 
    }
    return serviceAttributes;
  }

  public List<ServicesPackage> getServicesPackages(PerunSession sess) throws InternalErrorException {
    return getServicesManagerImpl().getServicesPackages(sess);
  }

  public ServicesPackage getServicesPackageById(PerunSession sess, int servicesPackageId) throws InternalErrorException, ServicesPackageNotExistsException {
    return getServicesManagerImpl().getServicesPackageById(sess, servicesPackageId);
  }

  public ServicesPackage getServicesPackageByName(PerunSession sess, String name) throws InternalErrorException, ServicesPackageNotExistsException {
    return getServicesManagerImpl().getServicesPackageByName(sess, name);
  }

  public ServicesPackage createServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageExistsException {
    Utils.notNull(servicesPackage.getDescription(), "servicesPackage.getDescription()");
    Utils.notNull(servicesPackage.getName(), "servicesPackage.getName()");

    //check if servicesPackage with same name exists in perun
    try {
      ServicesPackage s = getServicesManagerImpl().getServicesPackageByName(sess, servicesPackage.getName());
      throw new ServicesPackageExistsException(s);
    } catch(ServicesPackageNotExistsException ex) { /* OK */ }

    getPerunBl().getAuditer().log(sess, "{} created.", servicesPackage);
    return getServicesManagerImpl().createServicesPackage(sess, servicesPackage);
  }

  public void updateServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
    Utils.notNull(servicesPackage.getDescription(), "servicesPackage.getDescription()");
    Utils.notNull(servicesPackage.getName(), "servicesPackage.getName()");
    getServicesManagerImpl().updateServicesPackage(sess, servicesPackage);
    getPerunBl().getAuditer().log(sess, "{} updated.", servicesPackage);
  }

  public void deleteServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, RelationExistsException {
    if(getServicesFromServicesPackage(sess, servicesPackage).isEmpty()) {
      getServicesManagerImpl().deleteServicesPackage(sess, servicesPackage);
      getPerunBl().getAuditer().log(sess, "{} deleted.", servicesPackage);
    } else {
      throw new RelationExistsException("There is one or more services in the services package. ServicesPackage=\"" + servicesPackage + "\"");
    }
  }

  public void addServiceToServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException {
    getServicesManagerImpl().addServiceToServicesPackage(sess, servicesPackage, service);
    getPerunBl().getAuditer().log(sess, "{} added to {}.", service, servicesPackage);
  }

  public void removeServiceFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyRemovedFromServicePackageException {
    getServicesManagerImpl().removeServiceFromServicesPackage(sess, servicesPackage, service);
    getPerunBl().getAuditer().log(sess, "{} removed from {}.", service, servicesPackage);
  }

  public List<Service> getServicesFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
    return getServicesManagerImpl().getServicesFromServicesPackage(sess, servicesPackage);
  }

  public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeAlreadyAssignedException {
    //check if attribute isn't already added 
    List<AttributeDefinition> requiredAttributes = getPerunBl().getAttributesManagerBl().getRequiredAttributesDefinition(sess, service);
    if(requiredAttributes.contains(attribute)) throw new AttributeAlreadyAssignedException(attribute);

    getServicesManagerImpl().addRequiredAttribute(sess, service, attribute);
    getPerunBl().getAuditer().log(sess, "{} added to {} as required attribute.", attribute, service);
  }

  public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeAlreadyAssignedException {
    getServicesManagerImpl().addRequiredAttributes(sess, service, attributes);
    getPerunBl().getAuditer().log(sess, "{} added to {} as required attributes.", attributes, service);
  }

  public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeNotAssignedException {
    getServicesManagerImpl().removeRequiredAttribute(sess, service, attribute);
    getPerunBl().getAuditer().log(sess, "{} removed from {} as required attribute.", attribute, service);
  }

  public void removeRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotAssignedException {
    getServicesManagerImpl().removeRequiredAttributes(sess, service, attributes);
    getPerunBl().getAuditer().log(sess, "{} removed from {} as required attributes.", attributes, service);
  }

  public void removeAllRequiredAttributes(PerunSession sess, Service service) throws InternalErrorException {
    getServicesManagerImpl().removeAllRequiredAttributes(sess, service);
    getPerunBl().getAuditer().log(sess, "All required attributes removed from {}.", service);
  }

  public Destination addDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyAssignedException {
    if(!getServicesManagerImpl().destinationExists(sess, destination)) {
      try { 
        //Try to get the destination without id
        destination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
      } catch(DestinationNotExistsException ex) {
        try {
          destination = createDestination(sess, destination);
        } catch(DestinationExistsException e) {
          throw new ConsistencyErrorException(e);
        }
      }
    }
    if(getServicesManagerImpl().destinationExists(sess, service, facility, destination)) throw new DestinationAlreadyAssignedException(destination);

    getServicesManagerImpl().addDestination(sess, service, facility, destination);
    getPerunBl().getAuditer().log(sess, "{} added to {} and {}.", destination, service, facility);
    return destination;
  }

  public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyRemovedException {
    if(!getServicesManagerImpl().destinationExists(sess, destination)) {
      try { 
        //Try to get the destination without id
        destination = getServicesManagerImpl().getDestination(sess, destination.getDestination(), destination.getType());
      } catch(DestinationNotExistsException ex) {
        throw new DestinationAlreadyRemovedException(destination);
      }
    }
    getServicesManagerImpl().removeDestination(sess, service, facility, destination);
    //TODO remove destination from destination taable if is not used anymore
    getPerunBl().getAuditer().log(sess, "{} removed from {} and {}.", destination, service, facility);
  }

  public Destination getDestinationById(PerunSession sess, int id) throws InternalErrorException, DestinationNotExistsException {
    return getServicesManagerImpl().getDestinationById(sess, id);
  }

  public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
    List<Destination> destinations = getServicesManagerImpl().getDestinations(sess, service, facility);

    return destinations;
  }

  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException{
    return getServicesManagerImpl().getAllRichDestinations(perunSession, facility);
  }
  
  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws InternalErrorException{
    return getServicesManagerImpl().getAllRichDestinations(perunSession, service);
  }
  
  public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws InternalErrorException{
    return getServicesManagerImpl().getRichDestinations(perunSession, facility, service);
  }
 
  public void removeAllDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
    getServicesManagerImpl().removeAllDestinations(sess, service, facility);
    //TODO remove destination from destination taable if is not used anymore
    getPerunBl().getAuditer().log(sess, "All destinations removed from {} and {}.", service, facility);
  }

  public void checkServiceExists(PerunSession sess, Service service) throws InternalErrorException, ServiceNotExistsException {
    getServicesManagerImpl().checkServiceExists(sess, service);
  }

  public void checkServicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException {
    getServicesManagerImpl().checkServicesPackageExists(sess, servicesPackage);
  }

  public Owner getOwner(PerunSession sess, Service service) throws InternalErrorException {
    int ownerId = getServicesManagerImpl().getOwnerId(sess, service);
    try {
      return getPerunBl().getOwnersManagerBl().getOwnerById(sess, ownerId);
    } catch(OwnerNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }
  }

  public int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException {
    return servicesManagerImpl.getDestinationIdByName(sess, name);
  }

  public List<Service> getAssignedServices(PerunSession sess, Facility facility) throws InternalErrorException {
    return servicesManagerImpl.getAssignedServices(sess, facility);
  }

  public Destination createDestination(PerunSession sess, Destination destination) throws InternalErrorException, DestinationExistsException {
    if(getServicesManagerImpl().destinationExists(sess, destination)) throw new DestinationExistsException(destination);
    destination = getServicesManagerImpl().createDestination(sess, destination);
    getPerunBl().getAuditer().log(sess, "{} created.", destination);
    return destination;
  }


  /**
   * Gets the servicesManagerImpl.
   *
   * @return The servicesManagerImpl.
   */
  private ServicesManagerImplApi getServicesManagerImpl() {
    return this.servicesManagerImpl;
  }

  /**
   * Gets the perunBl.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }



  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  @Override
  public List<Destination> addDestinationsForAllServicesOnFacility(PerunSession sess, Facility facility, Destination destination)
      throws InternalErrorException, DestinationAlreadyAssignedException {
    List<Service> services = this.getAssignedServices(sess, facility);
    List<Destination> destinations = new ArrayList<Destination>();

    for (Service service: services) {
      destinations.add(this.addDestination(sess, service, facility, destination));
    }

    return destinations;
  }

  @Override
  public List<Destination> addDestinationsDefinedByHostsOnCluster(PerunSession sess, Service service, Facility facility)
      throws InternalErrorException, DestinationAlreadyAssignedException {

    // Get all hosts
    List<Host> hosts = getPerunBl().getFacilitiesManagerBl().getHosts(sess, facility);

    List<Destination> destinations = new ArrayList<Destination>();

    for (Host host: hosts) {
      if (host.getHostname() != null && !host.getHostname().isEmpty()) {
        Destination destination = new Destination();
        destination.setDestination(host.getHostname());
        destination.setType(Destination.DESTINATIONHOSTTYPE);
        destinations.add(this.addDestination(sess, service, facility, destination));
      }
    }

    return destinations;
  }
  
  public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws InternalErrorException {
    List<Destination> destinations = getServicesManagerImpl().getFacilitiesDestinations(sess, vo);
    return destinations;
  }
}
