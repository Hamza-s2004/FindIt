class DetailFragment : Fragment(R.layout.fragment_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.txtTitle)
        val location = view.findViewById<TextView>(R.id.txtLocation)
        val time = view.findViewById<TextView>(R.id.txtTime)

        // Get data from Bundle
        val bundle = arguments

        title.text = bundle?.getString("title")
        location.text = bundle?.getString("location")
        time.text = bundle?.getString("time")
    }
}