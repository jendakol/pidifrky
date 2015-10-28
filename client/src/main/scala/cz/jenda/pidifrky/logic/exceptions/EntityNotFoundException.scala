package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class EntityNotFoundException(entityDesc: String) extends PidifrkyException(s"Entity $entityDesc wasn't found")
