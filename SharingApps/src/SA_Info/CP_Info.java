package SA_Info;
// CP_Info: Client Program Info
public class CP_Info {
	public String prog_name;
	public String prog_info;
	public int arg_num;
	public String arg_info;
	public String user_id;
	public String user_name;
	public CP_Info(String user_id, String prog_name, String prog_info, 
			int arg_num, String arg_info) {
		this.user_id = user_id;
		this.prog_name = prog_name;
		this.prog_info = prog_info;
		this.arg_num = arg_num;
		this.arg_info = arg_info;
	}

	public String toString() {
		return prog_name;
	}
}

