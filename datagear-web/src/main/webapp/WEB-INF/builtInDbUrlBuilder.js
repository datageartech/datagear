{
	dbName : "MySQL",
	template : "jdbc:mysql://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 3306,
		name : ""
	}
},
{
	dbName : "PostgreSQL",
	template : "jdbc:postgresql://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 5432,
		name : ""
	}
},
{
	dbName : "Oracle",
	template : "jdbc:oracle:thin:@{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 1521,
		name : ""
	}
},
{
	dbName : "SQL Server",
	template : "jdbc:sqlserver://{host}:{port};DatabaseName={name}",
	defaultValue :
	{
		host : "",
		port : 1433,
		name : ""
	}
},
{
	dbName : "DB2",
	template : "jdbc:db2://{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 50000,
		name : ""
	}
},
{
	dbName : "Sybase",
	template : "jdbc:sybase:Tds:{host}:{port}/{name}",
	defaultValue :
	{
		host : "",
		port : 5000,
		name : ""
	}
}