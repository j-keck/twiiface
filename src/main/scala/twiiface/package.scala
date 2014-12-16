package twiiface {

}


package object twiiface {

  implicit class StringOps(val str: String) extends AnyVal {

    import java.net.URLEncoder

    def &(other: String) = s"$str&$other"
    def encode = URLEncoder.encode(str, "UTF-8")
    def bytes = str.getBytes("UTF-8")
  }

}