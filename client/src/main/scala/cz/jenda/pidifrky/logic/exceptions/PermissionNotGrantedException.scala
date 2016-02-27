package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class PermissionNotGrantedException(permissions: Array[String]) extends PidifrkyException(s"Permissions ${permissions.mkString(", ")} were not all granted")
