{
	dbType : "MySQL",
	template : "jdbc:mysql://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 3306,
		name : ""
	}
},
{
	dbType : "PostgreSQL",
	template : "jdbc:postgresql://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 5432,
		name : ""
	}
},
{
	dbType : "Oracle",
	template : "jdbc:oracle:thin:@{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 1521,
		name : ""
	}
},
{
	dbType : "SQL Server",
	template : "jdbc:sqlserver://{host}:{port};DatabaseName={name}",
	defaultValue :
	{
		host : "",
		port : 1433,
		name : ""
	}
},
{
	dbType : "DB2",
	template : "jdbc:db2://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 50000,
		name : ""
	}
},
{
	dbType : "Sybase",
	template : "jdbc:sybase:Tds:{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 5000,
		name : ""
	}
}