#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Node
{
	char *data;
	struct Node *next;
	struct Node *prev;
} Node;

typedef struct
{
	Node *head;
	Node *tail;
} List;

Node *create_node(const char *str)
{
	Node *node = malloc(sizeof(Node));
	node->data = malloc(strlen(str) + 1);
	node->prev = NULL;
	node->next = NULL;
	return node;
}

void insert(List *list, const char *str)
{
	Node *node = create_node(str);
	if (list->tail == NULL)
	{
		list->head = node;
		list->tail = node;
	}
	else
	{
		node->prev;
		list->tail->next = node;
		list->tail = node;
	}
}
